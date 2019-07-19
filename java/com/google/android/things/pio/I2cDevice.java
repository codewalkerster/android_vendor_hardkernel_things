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

import java.io.Closeable;

import java.io.IOException;

public interface I2cDevice extends Closeable {
    public abstract void close() throws IOException;
    public String getName();
    public abstract void read(byte[] buffer, int length) throws IOException, IllegalArgumentException;
    public abstract void readRegBuffer(int reg, byte[] buffer, int length) throws IOException, IllegalArgumentException;
    public abstract byte readRegByte(int reg) throws IOException, IllegalArgumentException;
    public abstract short readRegWord(int reg) throws IOException, IllegalArgumentException;
    public abstract void write(byte[] buffer, int length) throws IOException, IllegalArgumentException;
    public abstract void writeRegBuffer(int reg, byte[] buffer, int length) throws IOException, IllegalArgumentException;
    public abstract void writeRegByte(int reg, byte data) throws IOException, IllegalArgumentException;
    public abstract void writeRegWord(int reg, short data) throws IOException, IllegalArgumentException;
}
