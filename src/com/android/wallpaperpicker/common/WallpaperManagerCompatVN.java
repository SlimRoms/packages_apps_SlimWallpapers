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

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Rect;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

class WallpaperManagerCompatVN extends WallpaperManagerCompatV16 {
    WallpaperManagerCompatVN(Context context) {
        super(context);
    }

    @Override
    public void setStream(final InputStream data, Rect visibleCropHint, boolean allowBackup,
            int whichWallpaper) throws IOException {
        try {
            // TODO: use mWallpaperManager.setStream(data, visibleCropHint, allowBackup, which)
            // without needing reflection.
            Method setStream = WallpaperManager.class.getMethod("setStream", InputStream.class,
                    Rect.class, boolean.class, int.class);
            setStream.invoke(mWallpaperManager, data, visibleCropHint, allowBackup, whichWallpaper);
        } catch (Exception e) {
            // Fall back to previous implementation (set both)
            super.setStream(data, visibleCropHint, allowBackup, whichWallpaper);
        }
    }

    @Override
    public void clear(int whichWallpaper) throws IOException {
        try {
            // TODO: use mWallpaperManager.clear(whichWallpaper) without needing reflection.
            Method clear = WallpaperManager.class.getMethod("clear", int.class);
            clear.invoke(mWallpaperManager, whichWallpaper);
        } catch (Exception e) {
            // Fall back to previous implementation (set both)
            super.clear(whichWallpaper);
        }
    }
}
