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

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.wallpaperpicker.WallpaperPickerActivity;

import org.slim.wallpapers.R;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LiveWallpaperInfo extends WallpaperTileInfo {

    private static final String TAG = "LiveWallpaperTile";

    private Drawable mThumbnail;
    private WallpaperInfo mInfo;

    private LiveWallpaperInfo(Drawable thumbnail, WallpaperInfo info) {
        mThumbnail = thumbnail;
        mInfo = info;
    }

    @Override
    public void onClick(WallpaperPickerActivity a) {
        Intent preview = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        preview.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                mInfo.getComponent());
        a.startActivityForResultSafely(preview,
                WallpaperPickerActivity.PICK_WALLPAPER_THIRD_PARTY_ACTIVITY);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, ViewGroup parent) {
        mView = inflater.inflate(R.layout.wallpaper_picker_live_wallpaper_item, parent, false);

        ImageView image = (ImageView) mView.findViewById(R.id.wallpaper_image);
        ImageView icon = (ImageView) mView.findViewById(R.id.wallpaper_icon);
        if (mThumbnail != null) {
            image.setImageDrawable(mThumbnail);
            icon.setVisibility(View.GONE);
        } else {
            icon.setImageDrawable(mInfo.loadIcon(context.getPackageManager()));
            icon.setVisibility(View.VISIBLE);
        }

        TextView label = (TextView) mView.findViewById(R.id.wallpaper_item_label);
        label.setText(mInfo.loadLabel(context.getPackageManager()));
        return mView;
    }

    /**
     * An async task to load various live wallpaper tiles.
     */
    public static class LoaderTask extends AsyncTask<Void, Void, List<LiveWallpaperInfo>> {
        private final Context mContext;

        protected LoaderTask(Context context) {
            mContext = context;
        }

        @Override
        protected List<LiveWallpaperInfo> doInBackground(Void... params) {
            final PackageManager pm = mContext.getPackageManager();

            List<ResolveInfo> list = pm.queryIntentServices(
                    new Intent(WallpaperService.SERVICE_INTERFACE),
                    PackageManager.GET_META_DATA);

            Collections.sort(list, new Comparator<ResolveInfo>() {
                final Collator mCollator = Collator.getInstance();

                public int compare(ResolveInfo info1, ResolveInfo info2) {
                    return mCollator.compare(info1.loadLabel(pm), info2.loadLabel(pm));
                }
            });

            List<LiveWallpaperInfo> result = new ArrayList<>();

            for (ResolveInfo resolveInfo : list) {
                WallpaperInfo info;
                try {
                    info = new WallpaperInfo(mContext, resolveInfo);
                } catch (XmlPullParserException | IOException e) {
                    Log.w(TAG, "Skipping wallpaper " + resolveInfo.serviceInfo, e);
                    continue;
                }


                Drawable thumb = info.loadThumbnail(pm);
                Intent launchIntent = new Intent(WallpaperService.SERVICE_INTERFACE);
                launchIntent.setClassName(info.getPackageName(), info.getServiceName());
                result.add(new LiveWallpaperInfo(thumb, info));
            }

            return result;
        }
    }
}