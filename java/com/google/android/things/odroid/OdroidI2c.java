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
        return mI2cNative.read(idx, length);
    }

    public byte[] readRegBuffer(int reg, int length) {
        return mI2cNative.readRegBuffer(idx, reg, length);
    }

    public short readRegWord(int reg) {
        return mI2cNative.readRegWord(idx, reg);
    }

    public byte readRegByte(int reg) {
        return  mI2cNative.readRegByte(idx, reg);
    }

    public boolean write(byte[] buffer, int length) {
        return mI2cNative.write(idx, buffer, length);
    }

    public boolean writeRegBuffer(int reg, byte[] buffer, int length) {
        return mI2cNative.writeRegBuffer(idx, reg, buffer, length);
    }

    public boolean writeRegWord(int reg, short data) {
        return mI2cNative.writeRegWord(idx, reg, data);
    }

    public boolean writeRegByte(int reg, byte data) {
        return mI2cNative.writeRegByte(idx, reg, data);
    }

    private static class I2cNative {
        public void open(int i2cNameIdx, int address, int idx) {
            _open(i2cNameIdx, address, idx);
        }

        public void close(int idx) {
            _close(idx);
        }

        public byte[] read(int idx, int length) {
            return _read(idx, length);
        }

        public byte[] readRegBuffer(int idx, int reg, int length) {
            return _readRegBuffer(idx, reg, length);
        }

        public short readRegWord(int idx, int reg) {
            return _readRegWord(idx, reg);
        }

        public byte readRegByte(int idx, int reg) {
            return _readRegByte(idx, reg);
        }

        public boolean write(int idx, byte[] buffer, int length) {
            return _write(idx, buffer, length);
        }

        public boolean writeRegBuffer(int idx, int reg, byte[] buffer, int length) {
            return _writeRegBuffer(idx, reg, buffer, length);
        }

        public boolean writeRegWord(int idx, int reg, short data) {
            return _writeRegWord(idx, reg, data);
        }

        public boolean writeRegByte(int idx, int reg, byte data) {
            return _writeRegByte(idx, reg, data);
        }
    }

    private static native void _open(int i2cNameIdx, int address, int idx);
    private static native void _close(int idx);
    private static native byte[] _read(int idx, int length);
    private static native byte[] _readRegBuffer(int idx, int reg, int length);
    private static native short _readRegWord(int idx, int reg);
    private static native byte _readRegByte(int idx, int reg);
    private static native boolean _write(int idx, byte[] buffer, int length);
    private static native boolean _writeRegBuffer(int idx, int reg, byte[] buffer, int length);
    private static native boolean _writeRegWord(int idx, int reg, short data);
    private static native boolean _writeRegByte(int idx, int reg, byte data);
}
