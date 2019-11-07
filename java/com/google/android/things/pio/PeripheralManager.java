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
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.Pwm;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import android.util.Log;

import android.os.ServiceManager;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.Binder;

import com.google.android.things.pio.IThingsManager;
import com.google.android.things.pio.impl.GpioImpl;
import com.google.android.things.pio.impl.PwmImpl;

public final class PeripheralManager {
    private static final String TAG = "PeripheralManager";

    private static PeripheralManager sPeripheralManager;

    private IThingsManager mThingsManager;

    private final int thingsId;
    private Binder mBinder;

    private PeripheralManager() {
        Log.d(TAG, "PeripheralManager init");
        int id;
        mThingsManager = IThingsManager.Stub.asInterface(ServiceManager.getService("things"));
        try {
            mBinder = new Binder();
            id = mThingsManager.registNgetId(mBinder);
        } catch (RemoteException e) {
            Log.d(TAG, "getThingsId is not implemented");
            id = -1;
        }
        thingsId = id;
    }

    private PeripheralManager(IThingsManager manager, int thingsId) {
        this.thingsId = thingsId;
        mThingsManager = manager;
    }

    public static PeripheralManager getInstance() {
        synchronized (PeripheralManager.class) {
            if (sPeripheralManager == null) {
                sPeripheralManager = new PeripheralManager();
            }
            return sPeripheralManager;
        }
    }

    public List<String> getGpioList() {
        List<String> gpioList = null;
        try{
            gpioList = mThingsManager.getGpioList();
        } catch (RemoteException e) {
            Log.d(TAG, "Remote Exception!!");
        }
        return gpioList;
    }

    public List<String> getPwmList() {
        List<String> pwmList = null;
        try {
            pwmList = mThingsManager.getPwmList();
        } catch (RemoteException e) {
            Log.d(TAG, "getPwmList is not implemented");
        }
        return pwmList;
    }

    public Gpio openGpio(String name) throws IOException {
        Gpio gpio = null;
        try {
            int pin = mThingsManager.getGpioPinBy(name);
            if (pin != -1)
                gpio = new GpioImpl(name, pin, mThingsManager, thingsId);
        } catch (RemoteException e) {
            Log.d(TAG, "Remote Exception!!");
        }
        if (gpio == null)
            throw new IOException("gpio(" + name +") is not opened");

        return gpio;
    }

    public Pwm openPwm(String name) throws IOException {
        Pwm pwm = null;
        try {
            int pin = mThingsManager.getPwmPinBy(name);
            pwm = new PwmImpl(name, pin, mThingsManager, thingsId);
        } catch (RemoteException e) {
            Log.d(TAG, "openPwm is not implemented");
            return null;
        }
        if (pwm == null)
            throw new IOException("pwm(" + name +") is not opened");
        return pwm;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d(TAG,"PeripheralManager deconstructor");
        try {
            mThingsManager.releaseClient(thingsId);
        } catch (RemoteException e) {
            Log.d(TAG, "releaseThingsId is not implemented");
        }
        try {
            super.finalize();
        } catch (Exception e) {}
    }
}
