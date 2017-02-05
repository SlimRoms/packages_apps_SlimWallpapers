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

package com.android.wallpaperpicker.tileinfo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.slim.wallpapers.R;

/**
 * WallpaperTileInfo which uses drawable as the thumbnail.
 */
abstract class DrawableThumbWallpaperInfo extends WallpaperTileInfo {

    private final Drawable mThumb;

    DrawableThumbWallpaperInfo(Drawable thumb) {
        mThumb = thumb;
    }

    @Override
    public View createView(Context context, LayoutInflater inflator, ViewGroup parent) {
        mView = inflator.inflate(R.layout.wallpaper_picker_item, parent, false);
        setThumb(mThumb);
        return mView;
    }

    void setThumb(Drawable thumb) {
        if (mView != null && thumb != null) {
            ImageView image = (ImageView) mView.findViewById(R.id.wallpaper_image);
            image.setImageDrawable(thumb);
        }
    }
}
