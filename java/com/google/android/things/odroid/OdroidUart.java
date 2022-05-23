/*
 *    Copyright (c) 2020 Sangchul Go <luke.go@hardkernel.com>
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

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.things.odroid.Pin;
import com.google.android.things.pio.IUartDeviceCallback;
import com.google.android.things.pio.UartDevice;
/**
 * @hide
 */
public class OdroidUart implements Pin {
    private static final String TAG = "OdroidUart";
    private static final UartNative mUartNative = new UartNative();
    private RemoteCallbackList<IUartDeviceCallback> remoteCallback;
    private UartState state;
    private final Object mLock = new Object();

    private class UartState {
        UartState(int idx) {
            this.idx = idx;
        }
        public int idx;
    }

    public OdroidUart(int idx) {
        state = new UartState(idx);
        mUartNative.open(idx);
    }

    public Mode getCurrentMode() {
        return Mode.UART;
    }

    @Override
    public void close() {
        if (remoteCallback != null) {
            mUartNative.unregisterCallback(state.idx);
            remoteCallback.kill();
            remoteCallback = null;
        }
        mUartNative.close(state.idx);
        state = null;
    }

    public boolean flush(int direction) {
        return mUartNative.flush(state.idx, direction);
    }

    public boolean sendBreak(int duration) {
        return mUartNative.sendBreak(state.idx, duration);
    }

    public boolean setBaudrate(int rate) {
        return mUartNative.setBaudrate(state.idx, rate);
    }

    public boolean setDataSize(int dataSize) {
        return mUartNative.setDataSize(state.idx, dataSize);
    }

    public boolean setHardwareFlowControl(int flowControl) {
        return mUartNative.setHardwareFlowControl(state.idx, flowControl);
    }

    public boolean setParity(int parity) {
        return mUartNative.setParity(state.idx, parity);
    }

    public boolean setStopBits(int bits) {
        return mUartNative.setStopBits(state.idx, bits);
    }

    public byte[] read(int length) {
        return mUartNative.read(state.idx, length);
    }

    public int write(byte[] buffer, int length) {
        return mUartNative.write(state.idx, buffer, length);
    }

    public void registerCallback(IUartDeviceCallback callback) {
        synchronized(mLock) {
            remoteCallback = new RemoteCallbackList<IUartDeviceCallback>();
            remoteCallback.register(callback);
            mUartNative.registerCallback(state.idx);
        }
    }

    public void unregisterCallback(IUartDeviceCallback callback) {
        synchronized(mLock) {
            mUartNative.unregisterCallback(state.idx);
            remoteCallback.unregister(callback);
            remoteCallback = null;
        }
    }

    public void doCallback() {
        synchronized(mLock) {
            try {
                int callbackCount = remoteCallback.beginBroadcast();
                for (int i = 0; i < callbackCount; i++) {
                    IUartDeviceCallback callback = remoteCallback.getBroadcastItem(i);
                    try {
                        callback.onUartDeviceDataAvailable();
                    } catch (RemoteException e) {
                        Log.d(TAG, "Uart callback is not exit");
                    }
                }
                remoteCallback.finishBroadcast();
            } catch (IllegalStateException e) {
            }
        }
    }

    private static class UartNative {
        public void open(int idx) {
            _open(idx);
        }
        public void close(int idx) {
            _close(idx);
        }
        public boolean flush(int idx,int direction) {
            return _flush(idx, direction);
        }
        public boolean sendBreak(int idx,int duration) {
            return _sendBreak(idx, duration);
        }
        public boolean setBaudrate(int idx, int rate) {
            return _setBaudrate(idx, rate);
        }
        public boolean setDataSize(int idx, int size) {
            return _setDataSize(idx, size);
        }
        public boolean setHardwareFlowControl(int idx, int mode) {
            return _setHardwareFlowControl(idx, mode);
        }
        public boolean setParity(int idx, int mode) {
            return _setParity(idx, mode);
        }
        public boolean setStopBits(int idx, int bits) {
            return _setStopBits(idx, bits);
        }
        public byte[] read(int idx, int length) {
            return _read(idx, length);
        }
        public int write(int idx, byte[] buffer, int length) {
            return _write(idx, buffer, length);
        }
        public void registerCallback(int idx) {
            _registerCallback(idx);
        }
        public void unregisterCallback(int idx) {
            _unregisterCallback(idx);
        }
    }

    private static native void _open(int idx);
    private static native void _close(int idx);
    private static native boolean _flush(int idx, int direction);
    private static native boolean _sendBreak(int idx, int duration);
    private static native boolean _setBaudrate(int idx, int rate);
    private static native boolean _setDataSize(int idx, int size);
    private static native boolean _setHardwareFlowControl(int idx, int mode);
    private static native boolean _setParity(int idx, int mode);
    private static native boolean _setStopBits(int idx, int bits);
    private static native byte[] _read(int idx, int length);
    private static native int _write(int idx, byte[] buffer, int length);
    private static native void _registerCallback(int idx);
    private static native void _unregisterCallback(int idx);
}
