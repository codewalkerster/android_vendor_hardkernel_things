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

package com.google.android.things.pio.util;

import android.annotation.NonNull;
import android.os.Handler;

import java.util.concurrent.Executor;

/**
 * @hide
 */
public class CallbackHandlerExecutor implements Executor {
    private final Handler mHandler;
    private Runnable mRunnable;

    public CallbackHandlerExecutor(@NonNull Handler handler) {
        mHandler = handler;
    }

    public void setRunnable(Runnable runnable) {
        mRunnable = runnable;
    }

    @Override
    public void execute(Runnable command) {
        mHandler.post(command);
    }

    public void execute() {
        execute(mRunnable);
    }
}
