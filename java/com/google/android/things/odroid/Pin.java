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

/**
 * @hide
 */

public interface Pin {
    public abstract Mode getCurrentMode();
    public abstract void close();

    static public enum Mode {
        NONE,
        GPIO,
        I2C,
        PWM,
        SPI,
    }
}

class PinMode {
    static final int PWR = 1 << 0;
    static final int GND = 1 << 1;
    static final int GPIO = 1 << 2;
    static final int AIN = 1 << 3;
    static final int PWM = 1 << 4;
    static final int I2C = 1 << 5;
    static final int SPI = 1 << 6;
    static final int UART = 1 << 7;
    static final int ETC = 1 << 8;
}
