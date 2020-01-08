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

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import android.os.Binder;
import android.os.IBinder;

/**
 * @hide
 */
public class ThingsClientManager {
    private Map<Integer, ThingsClient> clientList;
    private OdroidThingsManager manager;

    public ThingsClientManager(OdroidThingsManager manager) {
        clientList = new HashMap<>();
        this.manager = manager;
    }

    public Collection<ThingsClient> clients() {
        return clientList.values();
    }

    public int create(IBinder binder) {
        int pid = Binder.getCallingPid();
        ThingsClient client = new ThingsClient(pid, binder, this);
        client.setThingsManager(manager);
        clientList.put(pid, client);

        return pid;
    }

    public void releaseClient(int id) {
        ThingsClient client = clientList.get(id);
        client.release();
        clientList.remove(id);
    }

    public void register(int pin, Device dev, int id) {
        ThingsClient client = clientList.get(id);
        switch (dev) {
            case GPIO:
                client.add(pin);
                break;
            case I2C:
                client.addI2c(pin);
                break;
        }
    }

    public void unregister(int pin, Device dev, int id) {
        ThingsClient client = clientList.get(id);
        switch (dev) {
            case GPIO:
                client.remove(pin);
                break;
            case I2C:
                client.removeI2c(pin);
                break;
        }
    }
}

enum Device {
    GPIO, PWM, I2C
}
