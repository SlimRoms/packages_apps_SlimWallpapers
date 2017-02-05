/**
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.wallpaperpicker.common;

import android.content.Context;
import android.graphics.Rect;

import java.io.IOException;
import java.io.InputStream;

public abstract class WallpaperManagerCompat {
    public static final int FLAG_SET_SYSTEM = 1 << 0; // TODO: use WallpaperManager.FLAG_SET_SYSTEM
    public static final int FLAG_SET_LOCK = 1 << 1; // TODO: use WallpaperManager.FLAG_SET_LOCK

    private static WallpaperManagerCompat sInstance;
    private static final Object sInstanceLock = new Object();

    public static WallpaperManagerCompat getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                if (Utilities.isAtLeastN()) {
                    sInstance = new WallpaperManagerCompatVN(context.getApplicationContext());
                } else {
                    sInstance = new WallpaperManagerCompatV16(context.getApplicationContext());
                }
            }
            return sInstance;
        }
    }

    public abstract void setStream(InputStream stream, Rect visibleCropHint, boolean allowBackup,
            int whichWallpaper) throws IOException;

    public abstract void clear(int whichWallpaper) throws IOException;
}
