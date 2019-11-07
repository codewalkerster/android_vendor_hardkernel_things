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

package com.google.android.things.pio.impl;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.IThingsManager;
import com.google.android.things.pio.IGpioCallback;
import com.google.android.things.pio.CallbackWrapper;


import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import java.io.Closeable;
import java.lang.AutoCloseable;
import java.io.IOException;
import android.util.Log;

/**
 * @hide
 */
public class GpioImpl implements Gpio,AutoCloseable {
    static final String TAG = "GpioImpl";
    final String name;
    final int pin;
    int direction;

    private final IThingsManager mThingsManager;
    private IGpioCallback mWrapperCallback;
    private final int thingsId;

    public GpioImpl(String name, int pin, IThingsManager manager, int thingsId) {
        this.name = name;
        this.pin = pin;
        mWrapperCallback = new CallbackWrapper(pin, manager, this);

        mThingsManager = manager;
        this.thingsId = thingsId;
        try {
            mThingsManager.register(pin, thingsId);
        } catch (RemoteException e) {}
    }

    @Override
    public void close() throws IOException {
        boolean result = false;
        try {
            result = mThingsManager.closeGpio(pin);
        } catch (RemoteException e) {
            Log.d(TAG, "Remote Exception!!");
        }
        if (result == false)
            throw new IOException("gpio is not closed");
        try {
            mThingsManager.unregister(pin, thingsId);
        } catch (RemoteException e) {}
    }

    public String getName() {
        return name;
    }

    public boolean getValue() {
        try {
            return mThingsManager.getGpioValue(pin);
        } catch (RemoteException e) {
            Log.d(TAG, "Remote Exception!!");
            return false;
        }
    }

    static Handler checkHandler(Handler handler) {
        if (handler == null) {
            Looper looper = Looper.myLooper();
            if (looper == null) {
                throw new IllegalArgumentException(
                        "No Handler given, and current thread has no looper!");
            }
            handler = new Handler(looper);
        }
        return handler;
    }


    public void registerGpioCallback(GpioCallback callback) throws IOException {
        registerGpioCallback(null, callback);
    }

    public void registerGpioCallback(Handler handler, GpioCallback callback) throws IOException {
        if (direction != DIRECTION_IN)
            throw new IOException("gpio should be input mode.");

        if (callback == null)
            throw new IOException("callback is null.");


        Handler runHandle = checkHandler(handler);
        // TODO: runHandler is not used. please use me!
        ((CallbackWrapper)mWrapperCallback).callback = callback;
        try {
        mThingsManager.registerGpioCallback(pin, mWrapperCallback);
        }catch(RemoteException e) {
        }
    }

    public void setActiveType(int activeType) throws IllegalArgumentException, IOException {
        if ((activeType != ACTIVE_LOW) &&
            (activeType != ACTIVE_HIGH)) {
            throw new IllegalArgumentException("active Type should be LOW or HIGH");
        }
        try {
            mThingsManager.setGpioActiveType(pin, activeType);
        } catch (RemoteException e) {
            Log.d(TAG, "Remote Exception!!");
        }
    }

    public void setDirection(int direction) throws IllegalArgumentException, IOException {
        if ((direction < DIRECTION_IN) ||
            (direction > DIRECTION_OUT_INITIALLY_LOW)) {
            throw new IllegalArgumentException("direction shuold be IN , OUT_INITIALLY_HIGH or OUT_INITIALLY_LOW");
        }
        this.direction = direction;
        try {
            mThingsManager.setGpioDirection(pin, direction);
        } catch (RemoteException e) {
            Log.d(TAG, "Remote Exception!!");
        }
    }

    public void setEdgeTriggerType(int edgeTriggerType) throws IllegalArgumentException, IOException {
        if ((edgeTriggerType < EDGE_NONE) || (edgeTriggerType > EDGE_BOTH)) {
            throw new IllegalArgumentException("edge trigger should be NONE, RISING, FALLING or BOTH");
        }
        if (direction != DIRECTION_IN)
            throw new IOException("set direction to the IN before set edge trigger type");

        try {
            mThingsManager.setEdgeTriggerType(pin, edgeTriggerType);
        } catch (RemoteException e) {
            Log.d(TAG, "Remote Exception!!");
        }
    }

    public void setValue(boolean value) throws IOException {
        if (direction == DIRECTION_IN)
            throw new IOException("set direction to the OUT before set value");
        try {
            mThingsManager.setGpioValue(pin, value);
        } catch (RemoteException e) {
            Log.d(TAG, "Remote Exception!!");
        }
    }

    public void unregisterGpioCallback(GpioCallback callback) {
        if (((CallbackWrapper)mWrapperCallback).callback == callback) {
            try {
                mThingsManager.unregisterGpioCallback(pin, mWrapperCallback);
                ((CallbackWrapper)mWrapperCallback).callback = null;
            } catch (RemoteException e) {
                Log.d(TAG, "Remote Exception!!");
            }
        }
    }
}
