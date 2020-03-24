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

package com.google.android.things.pio;

import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;
import com.google.android.things.pio.IUartDeviceCallback;
import com.google.android.things.pio.IThingsManager;

import com.google.android.things.pio.util.CallbackHandlerExecutor;

import android.util.Log;
import android.os.Handler;

import android.os.RemoteException;
public class UartCallbackWrapper extends IUartDeviceCallback.Stub {
    public int idx;
    public UartDeviceCallback callback;
    public IThingsManager manager;
    public UartDevice uart;

    private IUartDeviceCallback self;
    private static class Lock{}
    private Lock mLock = new Lock();
    public CallbackHandlerExecutor executor;

    public UartCallbackWrapper(int idx, IThingsManager manager, UartDevice uart) {
        this.idx = idx;
        this.manager = manager;
        this.uart = uart;
        self = this;
    }

    public void setHandler(Handler handler) {
        executor = new CallbackHandlerExecutor(handler);
        executor.setRunnable(new Runnable() {
            public void run() {
                synchronized(mLock) {
                    if (callback != null) {
                        boolean result = callback.onUartDeviceDataAvailable(uart);

                        if (result == false) {
                            try {
                                manager.unregisterUartDeviceCallback(idx, self);
                                callback = null;
                                executor = null;
                            } catch (RemoteException e) {
                                Log.d("GPIO_CALLBACK", "things manager is not exist");
                            }
                        }
                    }
                }
            }
        });
    }

    public void onUartDeviceDataAvailable() {
        executor.execute();
    }

    public void onUartDeviceError(int error) {
        callback.onUartDeviceError(uart, error);
    }
}
