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

import com.google.android.things.pio.IGpioCallback;
import android.os.IBinder;
/**
 * @hide
 */
interface IThingsManager {
    /* gpio */
    List<String> getGpioList();
    int getGpioPinBy(String name);

    int registNgetId(IBinder binder);
    void releaseClient(int thingsId);
    void register(int pin, int thingsId);
    void unregister(int pin, int thingsId);

    boolean closeGpio(int pin);

    void setGpioDirection(int pin, int direction);
    void setGpioValue(int pin, boolean value);
    boolean getGpioValue(int pin);
    void setGpioActiveType(int pin, int activeType);
    void setEdgeTriggerType(int pin, int edgeTriggerType);

    void registerGpioCallback(int pin, IGpioCallback callback);
    void unregisterGpioCallback(int pin, IGpioCallback callback);

    /* pwm */
    List<String> getPwmList();
    int getPwmPinBy(String name);

    boolean closePwm(int pin);
    boolean setEnabled(int pin, boolean enabled);
    boolean setPwmDutyCycle(int pin, double duty_cycle);
    boolean setPwmFrequencyHz(int pin, double frequency);
}
