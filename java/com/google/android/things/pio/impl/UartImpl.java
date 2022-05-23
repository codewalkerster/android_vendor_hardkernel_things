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

package com.google.android.things.pio.impl;

import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;
import com.google.android.things.pio.IThingsManager;
import com.google.android.things.pio.IUartDeviceCallback;
import com.google.android.things.pio.UartCallbackWrapper;

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
public class UartImpl implements UartDevice, AutoCloseable {
    final static String TAG = "UartImpl";
    final String name;
    final int idx;

    private final IThingsManager mThingsManager;
    private IUartDeviceCallback mWrapperCallback;
    private final int thingsId;

    public UartImpl(String name, int idx, IThingsManager manager, int thingsId) {
        this.name = name;
        this.idx = idx;
        mWrapperCallback = new UartCallbackWrapper(idx, manager, this);

        mThingsManager = manager;
        this.thingsId = thingsId;
        try {
            mThingsManager.registerUart(idx, thingsId);
        } catch (RemoteException e) {}
    }

    @Override
    public void close()
        throws IOException {
        boolean result = false;

        try {
            result = mThingsManager.closeUartBy(idx);
        } catch (RemoteException e) {
            Log.d(TAG, "Remote Exception: "+ e.getMessage());
        }

        if (result == false)
            throw new IOException("uart is not closed");

        try {
            mThingsManager.unregisterUart(idx, thingsId);
        } catch (RemoteException e) {}
    }

    @Override
    public String getName() {
        return name;
    }

    public void flush(int direction)
        throws IOException {
        if (direction < FLUSH_IN || direction > FLUSH_IN_OUT)
            throw new IOException("Wrong direction");
        boolean result = false;

        try {
            result = mThingsManager.flush(idx, direction);
        } catch (RemoteException e) {
            Log.d(TAG, "flush is not implemented - " + e.getMessage());
        }

        if (result == false)
            throw new IOException("flush is failed");
    }

    public void sendBreak(int duration_msecs)
        throws IOException {
        if (duration_msecs < 0)
            throw new IOException("duration must be positive");
        boolean result = false;

        try {
                result = mThingsManager.sendBreak(idx, duration_msecs);
        } catch (RemoteException e) {
            Log.d(TAG, "sendBreak is not implemented");
        }

        if (result == false)
            throw new IOException("flush is failed");
    }

    public void setBaudrate(int rate)
        throws IOException, IllegalArgumentException {
        if (rate <= 0)
            throw new IllegalArgumentException("baudrate is negative or zero");
        boolean result = false;

        try {
            result =  mThingsManager.setBaudrate(idx, rate);
        } catch (RemoteException e) {
            Log.d(TAG, "setBaudrate is not implemented");
        }

        if (result == false)
            throw new IOException("setting a baudrate is failed");
    }

    public void setDataSize(int size)
        throws IOException, IllegalArgumentException {
        if (size <= 0)
            throw new IllegalArgumentException("size is negative or zero");
        boolean result = false;

        try {
            result = mThingsManager.setDataSize(idx, size);
        } catch (RemoteException e) {
            Log.d(TAG, "setDataSize is not implemented");
        }

        if (result == false)
            throw new IOException("setting the data size is failed");
    }

    public void setHardwareFlowControl(int mode)
        throws IOException {
        if (mode < HW_FLOW_CONTROL_NONE || mode > HW_FLOW_CONTROL_AUTO_RTSCTS)
            throw new IOException("flow control value should be NONE or AUTO RTSCTS");
        boolean result = false;

        // rts, cts is not supported
        if (mode == HW_FLOW_CONTROL_AUTO_RTSCTS)
            throw new IOException("HW FLOW CONTROL is not supported");

        try {
            result = mThingsManager.setHardwareFlowControl(idx, mode);
        } catch (RemoteException e) {
            Log.d(TAG, "setHardwareFlowControl is not implemented");
        }

        if (result == false)
            throw new IOException("setting the hardware flow control is failed");
    }

    public void clearModemControl(int lines)
        throws IOException {
        throw new IOException("Modem Control Feature is not supported");
    }

    public void setModemControl(int lines)
        throws IOException {
        throw new IOException("Modem Control Feature is not supported");
    }

    public void setParity(int mode)
        throws IOException {
        if (mode < PARITY_NONE || mode > PARITY_SPACE)
            throw new IOException("Wrong mode value");
        boolean result = false;

        try {
            result = mThingsManager.setParity(idx, mode);
        } catch (RemoteException e) {
            Log.d(TAG, "setParity is not implemented");
        }

        if (result == false)
            throw new IOException("setting the mode is failed");
    }

    public void setStopBits(int bits)
        throws IOException, IllegalArgumentException {
        if (bits < 0)
            throw new IOException("stop bits is negative");
        boolean result = false;

        try {
            result = mThingsManager.setStopBits(idx, bits);
        } catch (RemoteException e) {
            Log.d(TAG, "setStopBits is not implemented");
        }

        if (result == false)
            throw new IOException("setting the stop bits is failed");
    }

    public int read(byte[] buffer, int length)
        throws IOException, IllegalArgumentException {
        int readBytes = 0;
        if (buffer.length < length)
            throw new IllegalArgumentException("length is bigger then buffer size");
        try {
            byte[] result = mThingsManager.readUart(idx, length);
            readBytes = result.length;
            for (int i=0; i< readBytes; i++)
                buffer[i] = result[i];
        } catch (RemoteException e) {}

        return readBytes;
    }

    public int write(byte[] buffer, int length)
        throws IOException, IllegalArgumentException {
        if (buffer.length < length)
            throw new IllegalArgumentException("length is bigger then buffer size");
        int result = -1;

        try {
            result = mThingsManager.writeUart(idx, buffer, length);
        } catch (RemoteException e) {}

        if (result < 0)
            throw new IOException("Failed write to the uart");

        return result;
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

    public void registerUartDeviceCallback(UartDeviceCallback callback)
        throws IOException {
        registerUartDeviceCallback(null, callback);
    }

    public void registerUartDeviceCallback(Handler handler, UartDeviceCallback callback)
        throws IOException {
        if (callback == null)
            throw new IOException("callback is null");

        ((UartCallbackWrapper)mWrapperCallback).callback = callback;
        ((UartCallbackWrapper)mWrapperCallback).setHandler(checkHandler(handler));
        try {
            mThingsManager.registerUartDeviceCallback(idx, mWrapperCallback);
        } catch (RemoteException e) {
        }
    }

    public void unregisterUartDeviceCallback(UartDeviceCallback callback) {
        if (((UartCallbackWrapper)mWrapperCallback).callback == callback) {
            try {
                mThingsManager.unregisterUartDeviceCallback(idx, mWrapperCallback);
                ((UartCallbackWrapper)mWrapperCallback).callback = null;
                ((UartCallbackWrapper)mWrapperCallback).executor = null;
            } catch (RemoteException e) {
                Log.d(TAG, "Remote Exception");
            }
        }
    }
}
