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
        public WakeLock wakeLock;

        public ThingsInstance(int thingsId, IBinder listener) {
            occupiedPin = new HashSet<>();
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

        public void release() {
            Iterator pinIterator = occupiedPin.iterator();
            while (pinIterator.hasNext())
                closePinBy((Integer)pinIterator.next());
            occupiedPin.clear();
            occupiedPin = null;

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

    private static List<PinState> pinStateList;

    private void initPinStateList() {
        pinStateList = new ArrayList<PinState>();

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

    private native void _init();
    private native ArrayList<String> _getListOf(int mode);
    private native ArrayList<String> _getPinName();
}
