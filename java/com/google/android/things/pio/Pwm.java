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

public interface Pwm extends Closeable {
    public abstract void close();
    public String getName();
    public abstract void setEnabled(boolean enabled) throws IOException;
    public abstract void setPwmDutyCycle(double duty_cycle) throws IllegalArgumentException, IOException;/* {
        if ((duty_cycle < 0) ||
            (duty_cycle > 100)) {
            throw new IllegalArgumentException("duty_cycle should between 0 and 100");
        }
    }*/
    public abstract void setPwmFrequencyHz(double freq_hz) throws IllegalArgumentException, IOException;/* {
        if (freq_hz < 0)
            throw new IllegalArgumentException("frequency must be positive");
    }
	*/
}
