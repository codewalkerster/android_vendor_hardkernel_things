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

package com.google.android.things.userdriver;

import com.google.android.things.userdriver.sensor.UserSensor;

import android.util.Log;

import java.util.List;
import java.util.ArrayList;
public class UserDriverManager {
    private static UserDriverManager sUserDriverManager = null;
    private static String TAG = "UserDriverManager";

    private UserDriverManager() {
        Log.d(TAG, "UserDriverManager init");

        sensorList = new ArrayList<>();
    }

    public static UserDriverManager getInstance() {
        synchronized (UserDriverManager.class) {
            if (sUserDriverManager == null)
                sUserDriverManager = new UserDriverManager();
            return sUserDriverManager;
        }
    }

    private List<UserSensor> sensorList;

    public void registerSensor(UserSensor sensor) {
        sensorList.add(sensor);
    }

    public void unregisterSensor(UserSensor sensor) {
        boolean result = sensorList.remove(sensor);
        if (!result)
            throw new RuntimeException("sensor not found");
    }
}
