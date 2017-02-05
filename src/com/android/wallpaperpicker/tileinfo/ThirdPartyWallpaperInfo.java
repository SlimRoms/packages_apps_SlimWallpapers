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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.wallpaperpicker.WallpaperPickerActivity;
import com.android.wallpaperpicker.WallpaperUtils;

import org.slim.wallpapers.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ThirdPartyWallpaperInfo extends WallpaperTileInfo {

    private final ResolveInfo mResolveInfo;
    private final int mIconSize;

    private ThirdPartyWallpaperInfo(ResolveInfo resolveInfo, int iconSize) {
        mResolveInfo = resolveInfo;
        mIconSize = iconSize;
    }

    @Override
    public void onClick(WallpaperPickerActivity a) {
        final ComponentName itemComponentName = new ComponentName(
                mResolveInfo.activityInfo.packageName, mResolveInfo.activityInfo.name);
        Intent launchIntent = new Intent(Intent.ACTION_SET_WALLPAPER)
            .setComponent(itemComponentName)
            .putExtra(WallpaperUtils.EXTRA_WALLPAPER_OFFSET,
                    a.getWallpaperParallaxOffset());
        a.startActivityForResultSafely(
                launchIntent, WallpaperPickerActivity.PICK_WALLPAPER_THIRD_PARTY_ACTIVITY);
    }

    @Override
    public View createView(Context context, LayoutInflater inflator, ViewGroup parent) {
        mView = inflator.inflate(R.layout.wallpaper_picker_third_party_item, parent, false);

        TextView label = (TextView) mView.findViewById(R.id.wallpaper_item_label);
        label.setText(mResolveInfo.loadLabel(context.getPackageManager()));
        Drawable icon = mResolveInfo.loadIcon(context.getPackageManager());
        icon.setBounds(new Rect(0, 0, mIconSize, mIconSize));
        label.setCompoundDrawables(null, icon, null, null);
        return mView;
    }

    public static List<ThirdPartyWallpaperInfo> getAll(Context context) {
        ArrayList<ThirdPartyWallpaperInfo> result = new ArrayList<>();
        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.wallpaperItemIconSize);

        final PackageManager pm = context.getPackageManager();
        Intent pickImageIntent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        HashSet<String> excludePackages = new HashSet<>();
        // Exclude packages which contain an image picker
        for (ResolveInfo info : pm.queryIntentActivities(pickImageIntent, 0)) {
            excludePackages.add(info.activityInfo.packageName);
        }
        excludePackages.add(context.getPackageName());
        excludePackages.add("com.android.wallpaper.livepicker");

        final Intent pickWallpaperIntent = new Intent(Intent.ACTION_SET_WALLPAPER);
        for (ResolveInfo info : pm.queryIntentActivities(pickWallpaperIntent, 0)) {
            if (!excludePackages.contains(info.activityInfo.packageName)) {
                result.add(new ThirdPartyWallpaperInfo(info, iconSize));
            }
        }
        return result;
    }
}