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
import com.google.android.things.pio.IUartDeviceCallback;
import com.google.android.things.pio.CallbackWrapper;

import com.google.android.things.odroid.Pin;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import android.util.Log;
import android.os.IBinder;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;

/**
 * @hide
 */
public class OdroidThingsManager extends IThingsManager.Stub {

    static final String TAG = "OdroidThingsManager";

    class PinState {
        public Pin pin = null;
        public String name;
    }

    class I2cState extends PinState {
        public int address;
    }

    class UartState extends PinState {
    }

    class SpiState extends PinState {
    }

    private static List<PinState> pinStateList;
    private static Map<Integer, I2cState> i2cStateList;
    private static List<UartState> uartStateList;
    private static List<SpiState> spiStateList;

    private List<String> i2cList = null;
    private static int i2cIdx = 0;

    private ThingsClientManager clientManager;

    private void initPinStateList() {
        pinStateList = new ArrayList<PinState>();
        i2cStateList = new HashMap<>();
        uartStateList = new ArrayList<>();
        spiStateList = new ArrayList<>();
        i2cList = _getListOf(PinMode.I2C);

        List<String> pinNames = _getPinName();

        int size = pinNames.size();
        for (int i=0; i<size; i++) {
            PinState state = new PinState();
            state.pin = null;
            state.name = pinNames.get(i);
            pinStateList.add(state);
        }

        List<String> uartList = _getListOf(PinMode.UART);
        size = uartList.size();
        for (int i=0; i<size; i++) {
            UartState state = new UartState();
            state.pin = null;
            state.name = uartList.get(i);
            uartStateList.add(state);
        }

        List<String> spiList = _getListOf(PinMode.SPI);
        for (String spi: spiList) {
            SpiState state = new SpiState();
            state.pin = null;
            state.name = spi;;
            spiStateList.add(state);
        }
    }

    public OdroidThingsManager () {
        _init();
        initPinStateList();

        clientManager = new ThingsClientManager(this);
    }

    @Override
    public int registNgetId(IBinder listener) {
        return clientManager.create(listener);
    }

    public void releaseClient(int clientId) {
        clientManager.releaseClient(clientId);
    }

    public void register(int pin, int clientId) {
        clientManager.register(pin, Device.GPIO, clientId);
    }

    public void unregister(int pin, int clientId) {
        clientManager.unregister(pin, Device.GPIO, clientId);
    }

    public void registerI2c(int idx, int clientId) {
        clientManager.register(idx, Device.I2C, clientId);
    }

    public void unregisterI2c(int idx, int clientId) {
        clientManager.unregister(idx, Device.I2C, clientId);
    }

    public void registerUart(int idx, int clientId) {
        clientManager.register(idx, Device.UART, clientId);
    }

    public void unregisterUart(int idx, int clientId) {
        clientManager.unregister(idx, Device.UART, clientId);
    }

    public void registerSpi(int idx, int clientId) {
        clientManager.register(idx, Device.SPI, clientId);
    }

    public void unregisterSpi(int idx, int clientId) {
        clientManager.unregister(idx, Device.SPI, clientId);
    }

    private List<String> getFilteredListOf(int mode) {
        List<String> list = _getListOf(mode);
        Set<Integer> pinList;

        // can I use pinStateList or uartStateList?
        for(ThingsClient client: clientManager.clients()) {
            switch (mode) {
                case PinMode.SPI:
                    pinList = client.getOccupiedSpi();
                    break;
                case PinMode.UART:
                    pinList = client.getOccupiedUart();
                break;
                default: // GPIO, PWM, etc
                    pinList = client.getOccupiedPin();
                    break;
            }

            pinList.forEach(
                    (pin) -> {
                        if (list.contains(pin.toString())) list.remove(pin.toString());
                    });
        }
        return list;
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

    public boolean closePinBy(int idx) {
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

    public int getGpioActiveType(int pin) {
        OdroidGpio gpio = (OdroidGpio)pinStateList.get(pin).pin;
        return gpio.getActiveType();
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

    public boolean closeI2cBy(int idx) {
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

    //Uart
    public List<String> getUartList() {
        return getFilteredListOf(PinMode.UART);
    }

    public int getUartIdxBy(String name) {
        for (UartState uart: uartStateList) {
            if (uart.name.equals(name)) {
                if (uart.pin == null) {
                    int idx = uartStateList.indexOf(uart);
                    uart.pin = new OdroidUart(idx);
                    return idx;
                }
                break;
            }
        }
        return -1;
    }

    public boolean closeUartBy(int idx) {
        UartState uart = uartStateList.get(idx);
        if (uart.pin == null)
            return false;

        uart.pin.close();
        uart.pin = null;
        return true;
    }

    public boolean flush(int idx, int direction) {
       OdroidUart uart = (OdroidUart)uartStateList.get(idx).pin;
       return uart.flush(direction);
    }

    public boolean sendBreak(int idx, int duration) {
       OdroidUart uart = (OdroidUart)uartStateList.get(idx).pin;
       return uart.sendBreak(duration);
    }

    public boolean setBaudrate(int idx, int rate) {
       OdroidUart uart = (OdroidUart)uartStateList.get(idx).pin;
       return uart.setBaudrate(rate);
    }

    public boolean setDataSize(int idx, int size) {
       OdroidUart uart = (OdroidUart)uartStateList.get(idx).pin;
       return uart.setDataSize(size);
    }

    public boolean setHardwareFlowControl(int idx, int mode) {
       OdroidUart uart = (OdroidUart)uartStateList.get(idx).pin;
       return uart.setHardwareFlowControl(mode);
    }

    public boolean setParity(int idx, int mode) {
       OdroidUart uart = (OdroidUart)uartStateList.get(idx).pin;
       return uart.setParity(mode);
    }

    public boolean setStopBits(int idx, int bits) {
       OdroidUart uart = (OdroidUart)uartStateList.get(idx).pin;
       return uart.setStopBits(bits);
    }

    public boolean clearModemControl(int idx, int lines) {
        return false;
    }

    public boolean setModemControl(int idx, int lines) {
        return false;
    }

    public byte[] readUart(int idx, int length) {
       OdroidUart uart = (OdroidUart)uartStateList.get(idx).pin;
       return uart.read(length);
    }

    public int writeUart(int idx, byte[] buffer, int length) {
       OdroidUart uart = (OdroidUart)uartStateList.get(idx).pin;
       return uart.write(buffer, length);
    }

    public void registerUartDeviceCallback(int idx, IUartDeviceCallback callback) {
       OdroidUart uart = (OdroidUart)uartStateList.get(idx).pin;
       uart.registerCallback(callback);
    }

    public void unregisterUartDeviceCallback(int idx, IUartDeviceCallback callback) {
       OdroidUart uart = (OdroidUart)uartStateList.get(idx).pin;
       if (uart != null)
           uart.unregisterCallback(callback);
    }

    public List<String> getSpiList() {
        return getFilteredListOf(PinMode.SPI);
    }

    public int getSpiIdxBy(String name) {
        for(int idx=0; idx < spiStateList.size(); idx++) {
            SpiState spi = spiStateList.get(idx);
            if (spi.name.equals(name)) {
                if (spi.pin == null) {
                    spi.pin = new OdroidSpi(idx);
                    return idx;
                }
                break;
            }
        }
        return -1;
    }

    public boolean closeSpiBy(int idx) {
        SpiState spi = spiStateList.get(idx);
        if (spi.pin == null)
            return false;

        spi.pin.close();
        spi.pin = null;
        return true;
    }

    public boolean setBitJustification(int idx, int justification) {
        OdroidSpi spi = (OdroidSpi)spiStateList.get(idx).pin;
        return spi.setBitJustification(justification);
    }

    public boolean setBitsPerWord(int idx, int bitsPerWord) {
        OdroidSpi spi = (OdroidSpi)spiStateList.get(idx).pin;
        return spi.setBitsPerWord(bitsPerWord);
    }

    public boolean setCsChange(int idx, boolean change) {
        OdroidSpi spi = (OdroidSpi)spiStateList.get(idx).pin;
        return spi.setCsChange(change);
    }

    public boolean setDelay(int idx, int delayUs) {
        OdroidSpi spi = (OdroidSpi)spiStateList.get(idx).pin;
        return spi.setDelay(delayUs);
    }

    public boolean setSpiFrequency(int idx, int frequencyHz) {
        OdroidSpi spi = (OdroidSpi)spiStateList.get(idx).pin;
        return spi.setFrequency(frequencyHz);
    }

    public boolean setMode(int idx, int mode) {
        OdroidSpi spi = (OdroidSpi)spiStateList.get(idx).pin;
        return spi.setMode(mode);
    }

    public byte[] readSpi(int idx, int length) {
        OdroidSpi spi = (OdroidSpi)spiStateList.get(idx).pin;
        return spi.read(length);
    }

    public boolean writeSpi(int idx, byte[] data, int length) {
        OdroidSpi spi = (OdroidSpi)spiStateList.get(idx).pin;
        return spi.write(data, length);
    }

    public byte[] transferSpi(int idx, byte[] data, int length) {
        OdroidSpi spi = (OdroidSpi)spiStateList.get(idx).pin;
        return spi.transfer(data, length);
    }

    private native void _init();
    private native ArrayList<String> _getListOf(int mode);
    private native ArrayList<String> _getPinName();
}
