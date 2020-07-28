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

package com.google.android.things.odroid;

import com.google.android.things.odroid.Pin;

import com.google.android.things.pio.SpiDevice;
/**
 * @hide
 */
public class OdroidSpi implements Pin {
    private static final String TAG = "OdroidSpi";
    private static final SpiNative mSpiNative = new SpiNative();
    private int idx;

    public OdroidSpi(int idx) {
        this.idx = idx;
        mSpiNative.open(idx);
    }

    public Mode getCurrentMode() {
        return Mode.SPI;
    }

    @Override
    public void close() {
        mSpiNative.close(idx);
    }

    public boolean setBitJustification(int justification) {
        return mSpiNative.setBitJustification(idx, justification);
    }

    public boolean setBitsPerWord (int bitPerWord) {
        return mSpiNative.setBitsPerWord(idx, bitPerWord);
    }

    public boolean setCsChange(boolean change) {
        return mSpiNative.setCsChange(idx, change);
    }

    public boolean setDelay(int delay) {
        return mSpiNative.setDelay(idx, delay);
    }

    public boolean setFrequency(int frequency) {
        return mSpiNative.setFrequency(idx, frequency);
    }

    public boolean setMode(int mode) {
        return mSpiNative.setMode(idx, mode);
    }

    public byte[] read(int length) {
        return mSpiNative.read(idx, length);
    }

    public boolean write(byte[] buffer, int length) {
        return mSpiNative.write(idx, buffer, length);
    }

    public byte[] transfer(byte[] buffer, int length) {
        return mSpiNative.transfer(idx, buffer, length);
    }

    private static class SpiNative {
        public void open(int idx) {
            _open(idx);
        }
        public void close(int idx) {
            _close(idx);
        }
        public boolean setBitJustification(int idx, int justification) {
            return _setBitJustification(idx, justification);
        }
        public boolean setBitsPerWord(int idx, int bitsPerWord) {
            return _setBitsPerWord(idx, bitsPerWord);
        }
        public boolean setCsChange(int idx, boolean change) {
            return _setCsChange(idx, change);
        }
        public boolean setDelay(int idx, int delay) {
            return _setDelay(idx, delay);
        }
        public boolean setFrequency(int idx, int frequency) {
            return _setFrequency(idx, frequency);
        }
        public boolean setMode(int idx, int mode) {
            return _setMode(idx, mode);
        }
        public byte[] read(int idx, int length) {
            return _read(idx, length);
        }
        public boolean write(int idx, byte[] buffer, int length) {
            return _write(idx, buffer, length);
        }
        public byte[] transfer(int idx, byte[] buffer, int length) {
            return _transfer(idx, buffer, length);
        }
    }

    private static native void _open(int idx);
    private static native void _close(int idx);
    private static native boolean _setBitJustification(int idx, int justification);
    private static native boolean _setBitsPerWord(int idx, int word);
    private static native boolean _setCsChange(int idx, boolean change);
    private static native boolean _setDelay(int idx, int delay);
    private static native boolean _setFrequency(int idx, int frequency);
    private static native boolean _setMode(int idx, int mode);
    private static native byte[] _read(int idx, int length);
    private static native boolean _write(int idx, byte[] buffer, int length);
    private static native byte[] _transfer(int idx, byte[] buffer, int length);
}
