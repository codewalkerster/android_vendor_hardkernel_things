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

package com.google.android.things.odroid;

import com.google.android.things.odroid.Pin;

/**
 * @hide
 */
public class OdroidI2c implements Pin {
    private static final String TAG = "OdroidI2c";
    private static final I2cNative mI2cNative = new I2cNative();
    private int idx;
    private final String name;

    private static final int BYTE = 1;
    private static final int WORD = 2;

    public OdroidI2c(String name, int i2cNameIdx, int address, int idx) {
        this.idx = idx;
        this.name = name;
        mI2cNative.open(i2cNameIdx, address, idx);
    }

    public Mode getCurrentMode() {
        return Mode.I2C;
    }

    @Override
    public void close() {
        mI2cNative.close(idx);
    }

    public byte[] read(int length) {
        return mI2cNative.readRegBuffer(idx, 0, length);
    }

    public byte[] readRegBuffer(int reg, int length) {
        return mI2cNative.readRegBuffer(idx, reg, length);
    }

    public byte readRegByte(int reg) {
        byte[] buffer = mI2cNative.readRegBuffer(idx, reg, BYTE);
        byte result = buffer[0];
        return result;
    }

    public short readRegWord(int reg) {
        byte[] buffer = mI2cNative.readRegBuffer(idx, reg, WORD);
        short result = (short)(((buffer[1] & 0xFF)<<8)|(buffer[0] & 0xFF));
        return result;
    }

    public boolean write(byte[] buffer, int length) {
        return mI2cNative.writeRegBuffer(idx, 0, buffer, length);
    }

    public boolean writeRegBuffer(int reg, byte[] buffer, int length) {
        return mI2cNative.writeRegBuffer(idx, reg, buffer, length);
    }

    public boolean writeRegByte(int reg, byte data) {
        byte[] buffer = new byte[BYTE];
        buffer[0] = data;
        return mI2cNative.writeRegBuffer(idx, reg, buffer, BYTE);
    }

    public boolean writeRegWord(int reg, short data) {
        byte[] buffer = new byte[WORD];
        buffer[1] = (byte)((data & 0xFF00) >> 8);
        buffer[0] = (byte)(data & 0xFF);
        return mI2cNative.writeRegBuffer(idx, reg, buffer, WORD);
    }

    private static class I2cNative {
        public void open(int i2cNameIdx, int address, int idx) {
            _open(i2cNameIdx, address, idx);
        }

        public void close(int idx) {
            _close(idx);
        }

        public byte[] readRegBuffer(int idx, int reg, int length) {
            return _readRegBuffer(idx, reg, length);
        }

        public boolean writeRegBuffer(int idx, int reg, byte[] buffer, int length) {
            return _writeRegBuffer(idx, reg, buffer, length);
        }
    }

    private static native void _open(int i2cNameIdx, int address, int idx);
    private static native void _close(int idx);
    private static native byte[] _readRegBuffer(int idx, int reg, int length);
    private static native boolean _writeRegBuffer(int idx, int reg, byte[] buffer, int length);
}
