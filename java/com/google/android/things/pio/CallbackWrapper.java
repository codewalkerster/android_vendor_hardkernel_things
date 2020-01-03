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

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.IGpioCallback;
import com.google.android.things.pio.IThingsManager;

import com.google.android.things.pio.util.CallbackHandlerExecutor;

import android.util.Log;
import android.os.Handler;

import android.os.RemoteException;
public class CallbackWrapper extends IGpioCallback.Stub {
    public int pin;
    public GpioCallback callback;
    public IThingsManager manager;
    public Gpio gpio;

    private IGpioCallback self;
    private static class Lock{}
    private Lock mLock = new Lock();
    public CallbackHandlerExecutor executor;

    public CallbackWrapper(int pin, IThingsManager manager, Gpio gpio) {
        this.pin = pin;
        this.manager = manager;
        this.gpio = gpio;
        self = this;
    }

    public void setHandler(Handler handler) {
        executor = new CallbackHandlerExecutor(handler);
        executor.setRunnable(new Runnable() {
            public void run() {
                synchronized(mLock) {
                    if (callback != null) {
                        boolean result = callback.onGpioEdge(gpio);

                        if (result == false) {
                            try {
                                manager.unregisterGpioCallback(pin, self);
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

    @Override
    public void onGpioEdge() {
        executor.execute();
    }

    @Override
    public void onGpioError(int error) {
        callback.onGpioError(gpio, error);
    }
}
