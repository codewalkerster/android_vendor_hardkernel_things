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
public class OdroidPwm implements Pin {
    private static final String TAG = "OdroidPwm";
    private static final PwmNative mPwmNative = new PwmNative();
    private PwmState pwmState;

    private class PwmState {
        PwmState(int pin) {
            this.pin = pin;
        }
        public int pin;
        public double frequencyHz;
        public double cycleRate;
    }

    public OdroidPwm(int pin) {
        pwmState = new PwmState(pin);
        mPwmNative.open(pin);
    }

    public Mode getCurrentMode() {
        return Mode.PWM;
    }

    @Override
    public void close() {
        mPwmNative.close(pwmState.pin);
        pwmState = null;
    }

    public boolean setEnabled(boolean enabled) {
        if (pwmState.frequencyHz == 0)
            return false;
        return mPwmNative.setEnabled(pwmState.pin, enabled);
    }

    public boolean setDutyCycle(double duty_cycle) {
        pwmState.cycleRate = duty_cycle;
        return mPwmNative.setDutyCycle(pwmState.pin, pwmState.cycleRate);
    }

    public boolean setFrequencyHz(double frequency) {
        pwmState.frequencyHz = frequency;
        return mPwmNative.setFrequency(pwmState.pin, pwmState.frequencyHz);
    }

    private static class PwmNative {
        public void open(int pin) {
            _openPwm(pin);
        }
        public void close(int pin) {
            _closePwm(pin);
        }

        public boolean setEnabled(int pin, boolean enabled) {
            return _setPwmEnabled(pin, enabled);
        }

        public boolean setDutyCycle(int pin, double cycleRate) {
            return _setDutyCycle(pin, cycleRate);
        }

        public boolean setFrequency(int pin, double frequencyHz) {
            return _setFrequency(pin, frequencyHz);
        }
    }

    private static native void _openPwm(int pin);
    private static native void _closePwm(int pin);
    private static native boolean _setPwmEnabled(int pin, boolean enabled);
    private static native boolean _setDutyCycle(int pin, double cycleRate);
    private static native boolean _setFrequency(int pin, double frequencyHz);
}
