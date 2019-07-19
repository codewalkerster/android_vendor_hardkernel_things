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

public interface Gpio extends Closeable {
    public static final int ACTIVE_HIGH = 1;
    public static final int ACTIVE_LOW = 0;

    public static final int DIRECTION_IN = 0;
    public static final int DIRECTION_OUT_INITIALLY_HIGH = 1;
    public static final int DIRECTION_OUT_INITIALLY_LOW = 2;

    public static final int EDGE_NONE = 0;
    public static final int EDGE_RISING = 1;
    public static final int EDGE_FALLING = 2;
    public static final int EDGE_BOTH = 3;

    public abstract void close() throws IOException;
    public String getName();
    public abstract boolean getValue() throws IOException;
    public void registerGpioCallback(GpioCallback callback) throws IOException;
    public abstract void registerGpioCallback(Handler handler, GpioCallback callback) throws IOException;
    public abstract void setActiveType(int activeType) throws IllegalArgumentException, IOException;
    public abstract void setDirection(int direction) throws IllegalArgumentException, IOException;
    public abstract void setEdgeTriggerType(int edgeTriggerType) throws IllegalArgumentException, IOException;
    public abstract void setValue(boolean value) throws IOException;
    public abstract void unregisterGpioCallback(GpioCallback callback);
}
