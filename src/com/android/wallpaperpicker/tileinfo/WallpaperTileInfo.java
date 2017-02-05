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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.gallery3d.common.Utils;
import com.android.wallpaperpicker.WallpaperPickerActivity;
import com.android.wallpaperpicker.common.InputStreamProvider;

import org.slim.wallpapers.R;

public abstract class WallpaperTileInfo {

    View mView;

    public void onClick(WallpaperPickerActivity a) {}

    public void onSave(WallpaperPickerActivity a) {}

    public void onDelete(WallpaperPickerActivity a) {}

    public boolean isSelectable() { return false; }

    public boolean isNamelessWallpaper() { return false; }

    public void onIndexUpdated(CharSequence label) {
        if (isNamelessWallpaper()) {
            mView.setContentDescription(label);
        }
    }

    public abstract View createView(Context context, LayoutInflater inflator, ViewGroup parent);

    static Point getDefaultThumbSize(Resources res) {
        return new Point(res.getDimensionPixelSize(R.dimen.wallpaperThumbnailWidth),
                res.getDimensionPixelSize(R.dimen.wallpaperThumbnailHeight));

    }

    static Bitmap createThumbnail(InputStreamProvider streamProvider, Context context,
            int rotation, boolean leftAligned) {
        Point size = getDefaultThumbSize(context.getResources());
        int width = size.x;
        int height = size.y;
        Point bounds = streamProvider.getImageBounds();
        if (bounds == null) {
            return null;
        }

        Matrix rotateMatrix = new Matrix();
        rotateMatrix.setRotate(rotation);
        float[] rotatedBounds = new float[] { bounds.x, bounds.y };
        rotateMatrix.mapPoints(rotatedBounds);
        rotatedBounds[0] = Math.abs(rotatedBounds[0]);
        rotatedBounds[1] = Math.abs(rotatedBounds[1]);

        RectF cropRect = Utils.getMaxCropRect(
                (int) rotatedBounds[0], (int) rotatedBounds[1], width, height, leftAligned);
        return streamProvider.readCroppedBitmap(cropRect, width, height, rotation);
    }
}