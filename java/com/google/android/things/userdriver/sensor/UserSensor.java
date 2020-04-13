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

package com.google.android.things.userdriver.sensor;

import android.hardware.Sensor;
import java.util.UUID;

public class UserSensor {
    //private Sensor mSensor;
    private UserSensor() {
        //mSensor = new Sensor();
    }

    public final int getReportingMode() {
        return reportingMode;
    }

    public final int getMaxDelay() {
        return maxDelay;
    }

    public final int getMinDelay() {
        return minDelay;
    }

    public final int getType() {
        return type;
    }

    public final int getVersion() {
        return version;
    }

    public final float getMaxRange() {
        return maxRange;
    }

    public final float getPower() {
        return power;
    }

    public final float getResolution() {
        return resolution;
    }

    public final String getName() {
        return name;
    }

    public final String getStringType() {
        return stringType;
    }

    public final String getVendor() {
        return vendor;
    }

    public final UUID getUuid() {
        return uuid;
    }

    public final UserSensorDriver getDriver() {
        return driver;
    }

    private int type;
    private String stringType;
    private String name;
    private String vendor;
    private int version;
    private int reportingMode;
    private float maxRange;
    private float resolution;
    private float power;
    private int minDelay;
    private int maxDelay;
    private UUID uuid;

    private UserSensorDriver driver = null;

    public static class Builder {
        private UserSensor sensor;

        public Builder() {
            sensor = new UserSensor();
        }

        public UserSensor build() throws IllegalArgumentException {
            if (sensor == null) // should check sensor parameters
                throw new IllegalArgumentException(" sensor cannot be null");

            return sensor;
        }

        public UserSensor.Builder setName(String name) {
            if (name == null)
                throw new IllegalArgumentException("name cannot be null");
            sensor.name = name;

            return this;
        }

        public UserSensor.Builder setVendor(String vendor) throws IllegalArgumentException {
            if (vendor == null)
                throw new IllegalArgumentException("vendor cannot be null");
            sensor.vendor = vendor;

            return this;
        }

        public UserSensor.Builder setVersion(int version) {
            //if (version == null)
            //    throw new IllegalArgumentException("version cannot be null");
            sensor.version = version;

            return this;
        }

        public UserSensor.Builder setType(int type) throws IllegalArgumentException {
            if (type < 0 || type > Sensor.TYPE_DEVICE_PRIVATE_BASE)
                throw new IllegalArgumentException("type is not supported");
            sensor.type = type;

            return this;
        }

        public UserSensor.Builder setCustomType(int type, String stringType, int reportingMode)
            throws IllegalArgumentException {
            if (type < Sensor.TYPE_DEVICE_PRIVATE_BASE)
                throw new IllegalArgumentException("type must be greater than or equal to Sensor.TYPE_DEVICE_PRIVATE");
            if (stringType == null) // TODO:check reverse-domain format
                throw new IllegalArgumentException("stringType must be in reverse-domain format");
            sensor.type = type;
            sensor.stringType = stringType;
            sensor.reportingMode = reportingMode;

            return this;
        }

        public UserSensor.Builder setMaxRange(float maxRange) throws IllegalArgumentException {
            if (maxRange <= 0.0)
                throw new IllegalArgumentException(" maxRange must be positive");
            sensor.maxRange = maxRange;

            return this;
        }

        public UserSensor.Builder setResolution(float resolution) throws IllegalArgumentException{
            if (resolution <= 0.0)
                throw new IllegalArgumentException("resolution must be positive");
            sensor.resolution = resolution;

            return this;
        }

        public UserSensor.Builder setPower(float power) throws IllegalArgumentException {
            if (power < 0.0)
                throw new IllegalArgumentException(" power must be positive");
            sensor.power = power;

            return this;
        }

        public UserSensor.Builder setMinDelay(int minDelay) throws IllegalArgumentException {
            if (minDelay < 0)
                throw new IllegalArgumentException(" min must be greater than or equal to 0");
            sensor.minDelay = minDelay;

            return this;
        }

        public UserSensor.Builder setMaxDelay(int maxDelay) throws IllegalArgumentException {
            if (maxDelay < 0)
                throw new IllegalArgumentException(" max must not be negative");
            sensor.maxDelay = maxDelay;

            return this;
        }

        public UserSensor.Builder setUuid(UUID uuid) {
            sensor.uuid = uuid;

            return this;
        }

        public UserSensor.Builder setDriver(UserSensorDriver driver) throws IllegalArgumentException {
            if (driver == null)
                throw new IllegalArgumentException("driver cannot be null");
            sensor.driver = driver;

            return this;
        }
    }
}

