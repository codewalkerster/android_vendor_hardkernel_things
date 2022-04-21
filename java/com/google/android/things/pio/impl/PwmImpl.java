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

import com.google.android.things.pio.Pwm;
import com.google.android.things.pio.IThingsManager;

import java.lang.AutoCloseable;
import java.io.IOException;

import android.os.RemoteException;

import android.util.Log;
/**
 * @hide
 */
public class PwmImpl implements Pwm, AutoCloseable {
    static final String TAG = "PwmImpl";
    final String name;
    final int pin;
    private final IThingsManager mThingsManager;
    private final int thingsId;

    public PwmImpl(String name, int pin, IThingsManager manager, int thingsId) {
        this.name = name;
        this.pin = pin;
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
            result = mThingsManager.closePwm(pin);
        } catch (RemoteException e) {
            Log.d(TAG, "closePwm is not implemented");
        }
        if (result == false)
            throw new IOException("pwm is already closed");
        try {
            mThingsManager.unregister(pin, thingsId);
        } catch (RemoteException e) {}
    }

    public String getName() {
        return name;
    }

    public void setEnabled(boolean enabled) throws IOException {
        boolean result = false;
        try {
            result = mThingsManager.setEnabled(pin, enabled);
        } catch(RemoteException e) {
            Log.d(TAG, "setEnabled is not implemented");
        }
        if (result == false)
            throw new IOException("set enable is failed");
    }

    public void setPwmDutyCycle(double duty_cycle)
            throws IllegalArgumentException, IOException {
        if ((duty_cycle < 0) || (duty_cycle > 100)) {
            throw new IllegalArgumentException("duty_cycle should between 0 and 100");
        }

        boolean result = false;
        try {
            result = mThingsManager.setPwmDutyCycle(pin, duty_cycle);
        } catch (RemoteException e) {
            Log.d(TAG, "setPwmDutyCycle is not implemented");
        }

        if (result == false)
            throw new IOException("set duty cycle is failed");
    }

    public void setPwmFrequencyHz(double freq_hz)
            throws IllegalArgumentException, IOException {
        if (freq_hz < 0)
            throw new IllegalArgumentException("frequency must be positive");

        boolean result = false;
        try {
            result = mThingsManager.setPwmFrequencyHz(pin, freq_hz);
        } catch (RemoteException e) {
            Log.d(TAG, "setPwmFrequencyHz is not implemented");
        }

        if (result == false)
            throw new IOException("set frequency is failed");
    }
}
