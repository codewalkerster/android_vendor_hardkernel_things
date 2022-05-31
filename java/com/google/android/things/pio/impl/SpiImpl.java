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

import com.google.android.things.pio.SpiDevice;
import com.google.android.things.pio.IThingsManager;

import android.os.RemoteException;

import java.lang.AutoCloseable;
import java.io.IOException;
import android.util.Log;

/**
 * @hide
 */
public class SpiImpl implements SpiDevice, AutoCloseable {
    final static String TAG = "SpiImpl";
    final String name;
    final int idx;

    private final IThingsManager mThingsManager;
    private final int thingsId;

    public SpiImpl(String name, int idx, IThingsManager manager, int thingsId) {
        this.name = name;
        this.idx = idx;

        mThingsManager = manager;
        this.thingsId = thingsId;
        try {
            mThingsManager.registerSpi(idx, thingsId);
        } catch (RemoteException e) {}
    }

    @Override
    public void close()
        throws IOException {
        boolean result = false;

        try {
            result = mThingsManager.closeSpiBy(idx);
        } catch (RemoteException e) {
            Log.d(TAG, "Remote Exception: "+ e.getMessage());
        }

        if (result == false)
            throw new IOException("spi is not closed");

        try {
            mThingsManager.unregisterSpi(idx, thingsId);
        } catch (RemoteException e) {}
    }

    @Override
    public String getName() {
        return name;
    }

    public void setBitJustification (int justification)
        throws IOException {
        if (justification != BIT_JUSTIFICATION_MSB_FISRT &&
                justification != BIT_JUSTIFICATION_LSB_FISRT)
            throw new IOException("justification value should LSB or MSB");
        boolean result = false;

        try {
            result = mThingsManager.setBitJustification(idx, justification);
        } catch (RemoteException e) {
            Log.d(TAG, "setBitJustification is not implemented");
        }

        if (result == false)
            throw new IOException("set justification failed");
    }

    public void setBitsPerWord (int bitsPerWord)
        throws IOException {
        if (bitsPerWord < 1)
            throw new IOException("bits per word must be positive");
        boolean result = false;

        try {
            result = mThingsManager.setBitsPerWord(idx, bitsPerWord);
        } catch (RemoteException  e) {
            Log.d(TAG, "bitsPerWord is not implemented");
        }

        if (result == false)
            throw new IOException("set bits per word failed");
    }

    public void setCsChange (boolean change)
        throws IOException {
        boolean result = false;

        try {
            result = mThingsManager.setCsChange(idx, change);
        } catch (RemoteException e) {
            Log.d(TAG, "set is not implemented");
        }

        if (result == false)
            throw new IOException("set cs change failed");
    }

    public void setDelay (int delayUs)
        throws IOException {
        if (delayUs < 0)
            throw new IOException("delay must be positive");
        boolean result = false;

        try {
            result = mThingsManager.setDelay(idx, delayUs);
        } catch (RemoteException e) {
            Log.d(TAG, "setDelay is not implemented");
        }

        if (result == false)
            throw new IOException("set delay is failed");
    }

    public void setFrequency (int frequencyHz)
        throws IOException, IllegalArgumentException {
        if (frequencyHz < 0)
            throw new IllegalArgumentException("Frequency must be positive");
        boolean result = false;

        try {
            result = mThingsManager.setSpiFrequency(idx, frequencyHz);
        } catch (RemoteException e) {
            Log.e(TAG, "setFrequency is not implemented");
        }

        if (result == false)
            throw new IOException("set frequency is failed");
    }

    public void setMode(int mode) throws IOException {
        if (mode < MODE0 || MODE3 < mode)
            throw new IOException("mode should be between MODE0, MODE1, MODE2 and MODE3");
        boolean result = false;

        try {
            result = mThingsManager.setMode(idx, mode);
        } catch (RemoteException e) {
            Log.d(TAG, "setMode is not implemented");
        }

        if (result == false)
            throw new IOException("set Mode is failed");
    }

    public void transfer (byte[] txBuffer, byte[] rxBuffer, int length)
            throws IllegalArgumentException, IOException {

        if (txBuffer.length < length || rxBuffer.length < length)
            throw new IllegalArgumentException("length is bigger than buffers size");

        try {
            byte[] result = mThingsManager.transferSpi(idx, txBuffer, length);
            for (int i = 0; i < result.length; i++)
                rxBuffer[i] = result[i];
        } catch (RemoteException e) {
            Log.d(TAG, "Spi transfer is not implemented");
        }
    }

    public void write(byte[] txBuffer, int length)
            throws IllegalArgumentException, IOException {
        boolean result = false;
        if (txBuffer.length < length)
            throw new IllegalArgumentException("length is bigger than buffer size");

        try {
            result = mThingsManager.writeSpi(idx, txBuffer, length);
        } catch (RemoteException e) {
            Log.d(TAG, "Spi write is not implemented");
        }

        if (result == false)
            throw new IOException("write is failed (Invalid speed or etc problem)");
    }

    public void read (byte[] rxBuffer, int length)
            throws IOException, IllegalArgumentException {
        if (rxBuffer.length < length)
            throw new IllegalArgumentException("length is bigger than buffer size");

        try {
            byte[] result = mThingsManager.readSpi(idx, length);
            for (int i = 0; i < result.length; i++)
                rxBuffer[i] = result[i];
        } catch (RemoteException e) {
            Log.d(TAG, "Spi read is not implemented");
        }
    }
}
