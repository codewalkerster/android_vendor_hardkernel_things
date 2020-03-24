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

package com.google.android.things.pio;

import android.os.Handler;

import java.io.Closeable;
import java.io.IOException;

public interface UartDevice extends Closeable {
    /* Queue Option Constant */
    public static final int FLUSH_IN = 0;
    public static final int FLUSH_OUT = 1;
    public static final int FLUSH_IN_OUT = 2;

    /* Hardware Flow Control Constant */
    public static final int HW_FLOW_CONTROL_NONE = 0;
    public static final int HW_FLOW_CONTROL_AUTO_RTSCTS = 1;

    /* Mode Control Constant */
    public static final int MODEM_CONTROL_LE  = 1; // 1 << 0
    public static final int MODEM_CONTROL_DTR = 2; // 1 << 1
    public static final int MODEM_CONTROL_RTS = 4; // 1 << 2
    public static final int MODEM_CONTROL_ST  = 8; // 1 << 3
    public static final int MODEM_CONTROL_SR  = 16; // 1 << 4
    public static final int MODEM_CONTROL_CTS = 32; // 1 << 5
    public static final int MODEM_CONTROL_CD  = 64; // 1 << 6
    public static final int MODEM_CONTROL_RI  = 128; // 1 << 7
    public static final int MODEM_CONTROL_DSR = 256; // 1 << 8

    /* Parity Constant */
    public static final int PARITY_NONE = 0;
    public static final int PARITY_EVEN = 1;
    public static final int PARITY_ODD = 2;
    public static final int PARITY_MARK = 3;
    public static final int PARITY_SPACE = 4;

    public abstract void clearModemControl(int lines) throws IOException;
    public abstract void close() throws IOException;
    public abstract void flush(int direction) throws IOException;
    public default String getName() {
        throw new RuntimeException("Stub!");
    }
    public abstract int read(byte[] buffer, int length) throws IOException;
    public default void registerUartDeviceCallback(UartDeviceCallback callback) throws IOException {
        throw new RuntimeException("Stub!");
    }
    public abstract void registerUartDeviceCallback(Handler handler, UartDeviceCallback callback) throws IOException;
    public abstract void sendBreak(int duration_msecs) throws IOException;
    public abstract void setBaudrate(int rate) throws IOException, IllegalArgumentException;
    public abstract void setDataSize(int size) throws IOException, IllegalArgumentException;
    public abstract void setHardwareFlowControl(int mode) throws IOException;
    public abstract void setModemControl(int lines) throws IOException;
    public abstract void setParity(int mode) throws IOException;
    public abstract void setStopBits(int bits) throws IOException;
    public abstract void unregisterUartDeviceCallback(UartDeviceCallback callback);
    public abstract int write(byte[] buffer, int length) throws IOException;
}
