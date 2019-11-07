/*
 *    Copyright (c) 2019 Sangchul Go <luke.go@hardkernel.com>
 *
 *    OdroidThings is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    OdroidThings is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with OdroidThings.
 *    If not, see <http://www.gnu.org/licenses/>.
 */

package com.google.android.things.odroid;

import com.google.android.things.odroid.Pin;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.IGpioCallback;
import com.google.android.things.pio.CallbackWrapper;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import android.util.Log;
/**
 * @hide
 */
public class OdroidGpio implements Pin {
    private static final String TAG = "OdroidGpio";
    private static final GpioNative mGpioNative = new GpioNative();
    private RemoteCallbackList<IGpioCallback> remoteCallback;
    private GpioState gpioState;
    private final Object mLock = new Object();

    class GpioState{
        GpioState(int pin) { this.pin = pin;}
        public int pin;
        public int direction;
        public int activeType;
        public int triggerType;
    }

    public OdroidGpio(int pin) {
        gpioState = new GpioState(pin);
        gpioState.activeType = Gpio.ACTIVE_HIGH;
    }

    @Override
    public Mode getCurrentMode() {
        return Mode.GPIO;
    }

    @Override
    public void close() {
        if (remoteCallback != null) {
            mGpioNative.unregisterCallback(gpioState.pin);
        }
    }

    public void setDirection(int direction) {
        gpioState.direction = direction;
        mGpioNative.setDirection(gpioState.pin, direction);
    }

    public void setValue(boolean value) {
        mGpioNative.setValue(gpioState.pin, value);
    }

    public boolean getValue() {
        boolean value = mGpioNative.getValue(gpioState.pin);
        if (gpioState.activeType == Gpio.ACTIVE_LOW)
            value =! value;
        return value;
    }

    public void setActiveType(int activeType) {
        gpioState.activeType = activeType;
        mGpioNative.setActiveType(gpioState.pin, activeType);
    }

    public void setEdgeTriggerType(int edgeTriggerType) {
        gpioState.triggerType = edgeTriggerType;
        mGpioNative.setEdgeTriggerType(gpioState.pin, edgeTriggerType);
    }

    public void registerCallback(IGpioCallback callback) {
        synchronized(mLock) {
            remoteCallback = new RemoteCallbackList<IGpioCallback>();
            remoteCallback.register(callback);
            mGpioNative.registerCallback(gpioState.pin);
        }
    }

    public void unregisterCallback(IGpioCallback callback) {
        synchronized(mLock) {
            mGpioNative.unregisterCallback(gpioState.pin);
            remoteCallback.unregister(callback);
            remoteCallback = null;
        }
    }

    public void doCallback() {
        synchronized(mLock) {
            try {
                int callbackCount = remoteCallback.beginBroadcast();
                for (int i=0; i<callbackCount; i++) {
                    IGpioCallback callback = remoteCallback.getBroadcastItem(i);
                    try {
                        callback.onGpioEdge();
                    } catch (RemoteException e) {
                        Log.d(TAG, "callback is not exit");
                    }
                }
                remoteCallback.finishBroadcast();
            } catch(IllegalStateException e) {
            }
        }
    }

    private static class GpioNative {
        public void setDirection(int pin, int direction) {
            _setGpioDirection(pin, direction);
        }

        public void setValue(int pin, boolean value) {
            _setGpioValue(pin, value);
        }

        public boolean getValue(int pin) {
            return _getGpioValue(pin);
        }

        public void setActiveType(int pin, int activeType) {
            _setGpioActiveType(pin, activeType);
        }

        public void setEdgeTriggerType(int pin, int edgeTriggerType) {
            _setEdgeTriggerType(pin, edgeTriggerType);
        }

        public void registerCallback(int pin) {
            _registerCallback(pin);
        }

        public void unregisterCallback(int pin) {
            _unregisterCallback(pin);
        }
    }

    private static native void _setGpioDirection(int pin, int direction);
    private static native void _setGpioValue(int pin, boolean value);
    private static native boolean _getGpioValue(int pin);
    private static native void _setGpioActiveType(int pin, int activeType);
    private static native void _setEdgeTriggerType(int pin, int edgeTriggerType);
    private static native void _registerCallback(int pin);
    private static native void _unregisterCallback(int pin);
}
