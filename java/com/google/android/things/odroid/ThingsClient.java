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

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import android.os.IBinder;
import android.util.Log;

/**
 * @hide
 */
public class ThingsClient {

    private static final String TAG = "OdroidThingsClient";
    private Set<Integer> occupiedPin;
    private Set<Integer> usedI2c;
    private Set<Integer> occupiedUart;
    private Set<Integer> occupiedSpi;
    private ClientBinder binder;
    private OdroidThingsManager manager;

    private final class ClientBinder implements IBinder.DeathRecipient {
        private final IBinder mListenr;
        private final int mClientId;
        private  ThingsClientManager clientManager;

        public ClientBinder(IBinder listener, int clientId, ThingsClientManager manager) {
            mListenr = listener;
            mClientId = clientId;
            clientManager = manager;
        }

        @Override
        public void binderDied() {
            mListenr.unlinkToDeath(this, 0);
            clientManager.releaseClient(mClientId);
            Log.d(TAG, "death things manager clientId- " + mClientId);
        }
    }

    public ThingsClient (int clientId, IBinder listener, ThingsClientManager manager) {
        occupiedPin = new HashSet<>();
        usedI2c = new HashSet<>();
        occupiedUart = new HashSet<>();
        occupiedSpi = new HashSet<>();
        binder = new ClientBinder(listener, clientId, manager);
        try {
            listener.linkToDeath(binder, 0);
        } catch(Exception e) {}
    }

    public void setThingsManager(OdroidThingsManager manager) {
        this.manager = manager;
    }

    public Set<Integer> getOccupiedPin() {
        return occupiedPin;
    }

    public Set<Integer> getOccupiedUart() {
        return occupiedUart;
    }

    public Set<Integer> getOccupiedSpi() {
        return occupiedSpi;
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

    public void addUart(int idx) {
        occupiedUart.add(idx);
    }

    public void removeUart(int idx) {
        occupiedUart.remove(idx);
    }

    public void addSpi(int idx) {
        occupiedSpi.add(idx);
    }

    public void removeSpi(int idx) {
        occupiedSpi.remove(idx);
    }

    public void release() {
        Iterator pinIterator = occupiedPin.iterator();
        while (pinIterator.hasNext())
            manager.closePinBy((Integer)pinIterator.next());
        occupiedPin.clear();
        occupiedPin = null;

        Iterator i2cIterator = usedI2c.iterator();
        while (i2cIterator.hasNext())
            manager.closeI2cBy((Integer)i2cIterator.next());
        usedI2c.clear();
        usedI2c = null;

        Iterator uartIterator = occupiedUart.iterator();
        while (uartIterator.hasNext())
            manager.closeUartBy((Integer)uartIterator.next());
        occupiedUart.clear();
        occupiedUart = null;

        Iterator spiIterator = occupiedSpi.iterator();
        while (spiIterator.hasNext())
            manager.closeSpiBy((Integer)spiIterator.next());
        occupiedSpi.clear();
        occupiedSpi = null;

        //?? should check
        binder = null;
        manager = null;
    }
}
