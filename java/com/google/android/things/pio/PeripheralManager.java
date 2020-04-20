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
import com.google.android.things.pio.UartDevice;

import java.io.IOException;

import java.util.List;
import android.util.Log;

import android.os.ServiceManager;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.Binder;

import com.google.android.things.pio.IThingsManager;
import com.google.android.things.pio.impl.GpioImpl;
import com.google.android.things.pio.impl.PwmImpl;
import com.google.android.things.pio.impl.I2cImpl;
import com.google.android.things.pio.impl.UartImpl;

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

    public List<String> getI2cBusList() {
        List<String> i2cList = null;
        try {
            i2cList = mThingsManager.getI2cList();
        } catch (RemoteException e) {
            Log.d(TAG, "getI2cBusList is not implemented");
        }
        return i2cList;
    }

    public List<String> getUartDeviceList() {
        List<String> uartList = null;
        try {
            uartList = mThingsManager.getUartList();
        } catch (RemoteException e) {
            Log.d(TAG, "getUartDeviceList is not implemented");
        }
        return uartList;
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
            if (pin != -1)
                pwm = new PwmImpl(name, pin, mThingsManager, thingsId);
        } catch (RemoteException e) {
            Log.d(TAG, "openPwm is not implemented");
            return null;
        }
        if (pwm == null)
            throw new IOException("pwm(" + name +") is not opened");
        return pwm;
    }

    public I2cDevice openI2cDevice(String name, int address) throws IOException {
        I2cDevice i2c = null;
        try {
            int idx = mThingsManager.getI2cIdxBy(name, address);
            i2c = new I2cImpl(name, idx, mThingsManager, thingsId);
        } catch (RemoteException e) {
            Log.d(TAG, "openI2cDevice is not implemented");
            return null;
        }
        if (i2c == null)
            throw new IOException("I2cDevice(" + name +"-" + address + ") is not opened");
        return i2c;
    }

    public UartDevice openUartDevice(String name) throws IOException {
        UartDevice uart = null;
        try {
            int idx = mThingsManager.getUartIdxBy(name);
            if (idx != -1)
                uart = new UartImpl(name, idx, mThingsManager, thingsId);
        } catch (RemoteException e) {
            Log.d(TAG, "openUartDevice is not implemented");
            return null;
        }
        if (uart == null)
            throw new IOException("UartDevice(" + name +") is not opened");
        return uart;
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
