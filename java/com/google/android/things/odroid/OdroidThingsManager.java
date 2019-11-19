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

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.IThingsManager;
import com.google.android.things.pio.IGpioCallback;
import com.google.android.things.pio.CallbackWrapper;

import com.google.android.things.odroid.Pin;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import android.os.IBinder;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
/**
 * @hide
 */
public class OdroidThingsManager extends IThingsManager.Stub {

    static final String TAG = "OdroidThingsManager";

    private boolean isExistPid(int pid) { // TODO: Remove it when confirmed that is binderDied always called.
        boolean result = false;
        try {
            List<RunningAppProcessInfo> procs =
                ActivityManager.getService().getRunningAppProcesses();
            int N = procs.size();
            for (int i=0; i<N; i++) {
                RunningAppProcessInfo proc = procs.get(i);
                if (proc.pid == pid) {
                    result = true;
                    break;
                }
            }
        } catch (RemoteException e) {}
        return result;
    }

    private final class WakeLock implements IBinder.DeathRecipient {
        public final IBinder mListenr;
        public final int mClientId;

        public WakeLock(IBinder listener, int clientId) {
            mListenr = listener;
            mClientId = clientId;
        }

        @Override
        public void binderDied() {
            mListenr.unlinkToDeath(this, 0);
            releaseClient(mClientId);
            Log.d(TAG, "death things manager clientId- " + mClientId);
        }
    }

    // per Peripheral manager called values
    private class ThingsInstance {
        public Set<Integer> occupiedPin;
        public Set<Integer> usedI2c;
        public WakeLock wakeLock;

        public ThingsInstance(int thingsId, IBinder listener) {
            occupiedPin = new HashSet<>();
            usedI2c = new HashSet<>();
            wakeLock = new WakeLock(listener, thingsId);
            try {
                listener.linkToDeath(wakeLock, 0);
            } catch(Exception e) {}
        }

        public void add(int pin) {
            occupiedPin.add(pin);
        }

        public void remove(int pin) {
            occupiedPin.remove(pin);
        }

        public void addI2c(int idx) {
            usedI2c.add(idx);
        }

        public void removeI2c(int idx) {
            usedI2c.remove(idx);
        }

        public void release() {
            Iterator pinIterator = occupiedPin.iterator();
            while (pinIterator.hasNext())
                closePinBy((Integer)pinIterator.next());
            occupiedPin.clear();
            occupiedPin = null;

            Iterator i2cIterator = usedI2c.iterator();
            while (i2cIterator.hasNext())
                closeI2cBy((Integer)i2cIterator.next());
            usedI2c.clear();
            usedI2c = null;

            wakeLock = null;
        }
    }

    Map<Integer, ThingsInstance> instanceList;

    @Override
    public int registNgetId(IBinder listener) {
        int pid = Binder.getCallingPid();
        ThingsInstance instance = new ThingsInstance(pid, listener);
        instanceList.put(pid, instance);
        return pid;
    }

    public void releaseClient(int thingsId) {
        ThingsInstance instance = instanceList.get(thingsId);
        instance.release();
        instanceList.remove(thingsId);
    }

    public void register(int pin, int thingsId) {
        ThingsInstance instance = instanceList.get(thingsId);
        instance.add(pin);
    }

    public void unregister(int pin, int thingsId) {
        ThingsInstance instance = instanceList.get(thingsId);
        instance.remove(pin);
    }

    public void registerI2c(int idx, int thingsId) {
        ThingsInstance instance = instanceList.get(thingsId);
        instance.addI2c(idx);
    }

    public void unregisterI2c(int idx, int thingsId) {
        ThingsInstance instance = instanceList.get(thingsId);
        instance.removeI2c(idx);
    }

    private List<String> getFilteredListOf(int mode) {
        List<String> list = _getListOf(mode);

        for(ThingsInstance instance: instanceList.values()) {
            instance.occupiedPin.forEach(
                    (pin) -> {
                        if (list.contains(pin.toString())) list.remove(pin.toString());
                    });
        }
        return list;
    }

    class PinState {
        public Pin pin = null;
        public String name;
    }

    class I2cState extends PinState {
        public int address;
    }

    private static List<PinState> pinStateList;
    private static Map<Integer, I2cState> i2cStateList;
    private List<String> i2cList = null;
    private static int i2cIdx = 0;

    private void initPinStateList() {
        pinStateList = new ArrayList<PinState>();
        i2cStateList = new HashMap<>();
        i2cList = _getListOf(PinMode.I2C);

        List<String> pinNames = _getPinName();

        int size = pinNames.size();
        for (int i=0; i<size; i++) {
            PinState state = new PinState();
            state.pin = null;
            state.name = pinNames.get(i);
            pinStateList.add(state);
        }
    }

    public OdroidThingsManager () {
        _init();
        initPinStateList();

        instanceList = new HashMap<>();
    }

    private interface InitCallback {
        Pin initPinBy(int idx);
    }

    private int getPinNumBy(String name, InitCallback callback) {
        for (PinState pin:pinStateList) {
            if (pin.name.equals(name)) {
                if (pin.pin == null) {
                    int idx = pinStateList.indexOf(pin);
                    pin.pin = callback.initPinBy(idx);
                    return idx;
                }
                break;
            }
        }
        return -1;
    }

    private boolean closePinBy(int idx) {
        PinState pin = pinStateList.get(idx);
        if(pin.pin == null)
            return false;

        pin.pin.close();
        pin.pin = null;
        return true;
    }

    public List<String> getGpioList() {
        return getFilteredListOf(PinMode.GPIO);
    }

    public int getGpioPinBy(String name) {
        return getPinNumBy(name, new InitCallback() {
            @Override
            public Pin initPinBy(int idx) {
                return new OdroidGpio(idx);
            }
        });
    }

    public boolean closeGpio(int idx) {
        return closePinBy(idx);
    }

    public void setGpioDirection (int pin, int direction) {
        OdroidGpio gpio = (OdroidGpio)pinStateList.get(pin).pin;
        gpio.setDirection(direction);
    }

    public void setGpioValue(int pin, boolean value) {
        OdroidGpio gpio = (OdroidGpio)pinStateList.get(pin).pin;
        gpio.setValue(value);
    }

    public boolean getGpioValue(int pin) {
        OdroidGpio gpio = (OdroidGpio)pinStateList.get(pin).pin;
        return gpio.getValue();
    }

    public void setGpioActiveType(int pin, int activeType) {
        OdroidGpio gpio = (OdroidGpio)pinStateList.get(pin).pin;
        gpio.setActiveType(activeType);
    }

    public void setEdgeTriggerType(int pin, int edgeTriggerType) {
        OdroidGpio gpio = (OdroidGpio)pinStateList.get(pin).pin;
        gpio.setEdgeTriggerType(edgeTriggerType);
    }

    @Override
    public void registerGpioCallback(int pin, IGpioCallback callback) {
        OdroidGpio gpio = (OdroidGpio)pinStateList.get(pin).pin;
        gpio.registerCallback(callback);
    }

    @Override
    public void unregisterGpioCallback(int pin, IGpioCallback callback) {
        OdroidGpio gpio = (OdroidGpio)pinStateList.get(pin).pin;
        if (gpio != null)
            gpio.unregisterCallback(callback);
    }

    public static void doCallback(int pin) {
        OdroidGpio gpio = (OdroidGpio)pinStateList.get(pin).pin;
        gpio.doCallback();
    }

    // pwm
    public List<String> getPwmList() {
        return getFilteredListOf(PinMode.PWM);
    }

    public int getPwmPinBy(String name) {
        return getPinNumBy(name, new InitCallback() {
            @Override
            public Pin initPinBy(int idx) {
                return new OdroidPwm(idx);
            }
        });
    }

    public boolean closePwm(int idx) {
        return closePinBy(idx);
    }

    public boolean setEnabled(int pin, boolean enabled) {
        OdroidPwm pwm = (OdroidPwm)pinStateList.get(pin).pin;
        return pwm.setEnabled(enabled);
    }

    public boolean setPwmDutyCycle(int pin, double duty_cycle) {
        OdroidPwm pwm = (OdroidPwm)pinStateList.get(pin).pin;
        return pwm.setDutyCycle(duty_cycle);
    }

    public boolean setPwmFrequencyHz(int pin, double frequency) {
        OdroidPwm pwm = (OdroidPwm)pinStateList.get(pin).pin;
        return pwm.setFrequencyHz(frequency);
    }

    // i2c
    public List<String> getI2cList() {
        return i2cList;
    }

    public int getI2cIdxBy(String name, int address) {
        I2cState state = new I2cState();
        state.name = name;
        state.address = address;
        int listIdx = i2cList.indexOf(name);
        int idx = i2cIdx++;
        state.pin = new OdroidI2c(name, listIdx, address, idx);

        i2cStateList.put(idx, state);
        return idx;
    }

    private boolean closeI2cBy(int idx) {
        I2cState i2c = i2cStateList.get(idx);
        if (i2c == null)
            return false;

        i2c.pin.close();
        i2c.pin = null;
        i2c = null;
        i2cStateList.remove(idx);
        return true;
    }

    public boolean closeI2c(int idx) {
        return closeI2cBy(idx);
    }

    public byte[] readI2c(int idx, int length) {
        OdroidI2c i2c = (OdroidI2c)i2cStateList.get(idx).pin;
        return i2c.read(length);
    }

    public byte[] readI2cRegBuffer(int idx, int reg, int length) {
        OdroidI2c i2c = (OdroidI2c)i2cStateList.get(idx).pin;
        return i2c.readRegBuffer(reg, length);
    }

    public byte readI2cRegByte(int idx, int reg) {
        OdroidI2c i2c = (OdroidI2c)i2cStateList.get(idx).pin;
        return i2c.readRegByte(reg);
    }

    public int readI2cRegWord(int idx, int reg) {
        OdroidI2c i2c = (OdroidI2c)i2cStateList.get(idx).pin;
        return (int)i2c.readRegWord(reg);
    }

    public boolean writeI2c(int idx, byte[] buffer, int length) {
        OdroidI2c i2c = (OdroidI2c)i2cStateList.get(idx).pin;
        return i2c.write(buffer, length);
    }

    public boolean writeI2cRegBuffer(int idx, int reg, byte[] buffer, int length) {
        OdroidI2c i2c = (OdroidI2c)i2cStateList.get(idx).pin;
        return i2c.writeRegBuffer(reg, buffer, length);
    }

    public boolean writeI2cRegByte(int idx, int reg, byte data) {
        OdroidI2c i2c = (OdroidI2c)i2cStateList.get(idx).pin;
        return i2c.writeRegByte(reg, data);
    }

    public boolean writeI2cRegWord(int idx, int reg, int data) {
        OdroidI2c i2c = (OdroidI2c)i2cStateList.get(idx).pin;
        return i2c.writeRegWord(reg, (short)data);
    }

    private native void _init();
    private native ArrayList<String> _getListOf(int mode);
    private native ArrayList<String> _getPinName();
}
