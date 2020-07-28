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

import android.os.Handler;

import java.io.Closeable;
import java.io.IOException;


public interface SpiDevice extends Closeable {
    public static final int BIT_JUSTIFICATION_LSB_FISRT = 1;
    public static final int BIT_JUSTIFICATION_MSB_FISRT = 0;
    public static final int MODE0 = 0;
    public static final int MODE1 = 1;
    public static final int MODE2 = 2;
    public static final int MODE3 = 3;

    public abstract void close() throws IOException;
    public String getName();
    public abstract void setBitJustification (int justification) throws IOException;
    public abstract void setBitsPerWord (int bitsPerWord) throws IOException;
    public abstract void setCsChange (boolean chnage) throws IOException;
    public abstract void setDelay (int delayUs) throws IOException;
    public abstract void setFrequency (int frequencyHz) throws IOException, IllegalArgumentException;

    public abstract void setMode(int mode) throws IOException;
    public abstract void transfer (byte[] txBuffer, byte[] rxBuffer, int length) throws IllegalArgumentException, IOException;
    public void write(byte[] buffer, int length) throws IllegalArgumentException, IOException;
    public void read (byte[] buffer, int length) throws IllegalArgumentException, IOException;
}
