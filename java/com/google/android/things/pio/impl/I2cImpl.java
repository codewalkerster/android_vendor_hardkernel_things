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

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.IThingsManager;

import android.os.RemoteException;

import java.lang.AutoCloseable;
import java.io.IOException;
import android.util.Log;
/**
 * @hide
 */
public class I2cImpl implements I2cDevice, AutoCloseable {
    static final String TAG = "I2cImpl";
    final String name;
    final int idx;

    private final IThingsManager mThingsManager;
    private final int thingsId;

    public I2cImpl(String name, int idx, IThingsManager manager, int thingsId) {
        this.name = name;
        this.idx = idx;

        mThingsManager = manager;
        this.thingsId = thingsId;
        try {
            mThingsManager.registerI2c(idx, thingsId);
        } catch (RemoteException e) {}
    }

    @Override
    public void close() throws IOException {
        boolean result = false;
        try {
            result = mThingsManager.closeI2c(idx);
        } catch (RemoteException e) {
            Log.d(TAG, "Remote Exception!!");
        }
        if (result == false)
            throw new IOException("i2c is not closed");
        try {
            mThingsManager.unregisterI2c(idx, thingsId);
        } catch (RemoteException e) {}
    }

    public String getName() {
        return name;
    }

    public void read(byte[] buffer, int length) throws IOException, IllegalArgumentException {
        if (buffer.length < length)
            throw new IllegalArgumentException("length is bigger than buffer size");

        try {
            byte[] result = mThingsManager.readI2c(idx, length);
            for(int i=0;i < length; i++)
                buffer[i] = result[i];
        } catch (RemoteException e) { Log.d(TAG, "I2C read is not implemented!"); }
    }

    public void readRegBuffer(int reg, byte[] buffer, int length) throws IOException, IllegalArgumentException {
        if (buffer.length < length)
            throw new IllegalArgumentException("length is bigger than buffer size");

        try {
            byte[] result = mThingsManager.readI2cRegBuffer(idx, reg, length);
            for(int i=0; i < length ;i ++)
                buffer[i] = result[i];
        } catch (RemoteException e) {}
    }

    public byte readRegByte(int reg) throws IOException, IllegalArgumentException {
        byte result = 0;
        try {
            result = mThingsManager.readI2cRegByte(idx, reg);
        } catch (RemoteException e) {}
        return result;
    }

    public short readRegWord(int reg) throws IOException, IllegalArgumentException {
        short result = 0;
        try {
            result = (short)mThingsManager.readI2cRegWord(idx, reg);
        } catch (RemoteException e) {}
        return result;
    }

    public void write(byte[] buffer, int length) throws IOException, IllegalArgumentException {
        boolean status = false;
        if (buffer.length < length)
            throw new IllegalArgumentException("length is bigger than buffer size");

        try {
            status = mThingsManager.writeI2c(idx, buffer, length);
        } catch (RemoteException e) {}

        if (status == false)
            throw new IOException("write is failed");
    }

    public void writeRegBuffer(int reg, byte[] buffer, int length) throws IOException, IllegalArgumentException {
        boolean status = false;
        if (buffer.length < length)
            throw new IllegalArgumentException("length is bigger than buffer size");

        try {
            status = mThingsManager.writeI2cRegBuffer(idx, reg, buffer, length);
        } catch (RemoteException e) {}

        if (status == false)
            throw new IOException("write is failed");
    }

    public void writeRegByte(int reg, byte data) throws IOException, IllegalArgumentException {
        boolean status = false;
        try {
            status = mThingsManager.writeI2cRegByte(idx, reg, data);
        } catch (RemoteException e) {}

        if (status == false)
            throw new IOException("write is failed");
    }

    public void writeRegWord(int reg, short data) throws IOException, IllegalArgumentException {
        boolean status = false;
        try {
            status = mThingsManager.writeI2cRegWord(idx, reg, (int)data);
        } catch (RemoteException e) {}

        if (status == false)
            throw new IOException("write is failed");
    }
}
