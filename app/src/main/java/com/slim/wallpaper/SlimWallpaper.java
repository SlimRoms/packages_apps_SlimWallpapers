/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.slim.wallpaper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class SlimWallpaper extends Activity {

    private static final String SETTINGS = "com.slim.wallpaper_prefs";
    private static final String LAST_WALLPAPER = "last_wallpaper";


    private HorizontalLayout mLayout;
    private ImageView mImageView;
    private boolean mIsWallpaperSet;

    private Bitmap mBitmap;

    private ArrayList<Drawable> mThumbs;
    private ArrayList<Integer> mImages;
    private static WallpaperLoader mLoader;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        findWallpapers();

        setContentView(R.layout.wallpaper_chooser);

        int last = getLastWallpaper();
        mLayout = (HorizontalLayout) findViewById(R.id.gallery);
        mLayout.setDefault(last);
        imageClicked(last);
        mLayout.setOnImageClickListener(new HorizontalLayout.OnImageClickListener() {
            @Override
            public void onImageClick(View v) {
                imageClicked(v.getId());
            }
        });
        for (int i = 0; i < mThumbs.size(); i++) {
            mLayout.add(mThumbs.get(i), i);
        }
        mImageView = (ImageView) findViewById(R.id.wallpaper);
        new ImageResizer().execute(R.array.wallpapers);
        new ImageResizer().execute(R.array.extra_wallpapers);
    }

    private void imageClicked(int i) {
        if (mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            mLoader.cancel();
        }
        mLoader = (WallpaperLoader) new WallpaperLoader().execute(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.apply, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.apply:
                selectWallpaper(mLayout.getCurrent());
                setLastWallpaper(mLayout.getCurrent());
                break;

            case R.id.save:
                try {
                    saveWallpaper(mLayout.getCurrent());
                } catch (IOException e) {
                    e.printStackTrace();
                    makeToast("Failed to make folder");
                }
                break;
        }
        return true;
    }

    private void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void saveWallpaper(int position) throws IOException {
        Bitmap b = BitmapFactory.decodeResource(getResources(), mImages.get(position));
        int[] size = getWallpaperSize();
        Bitmap bi = ImageHelper.resize(getApplicationContext(), b, size[0], size[1]);
        File folder = new File(Environment.getExternalStorageDirectory() + "/Slim/wallpapers");
        String file = folder + "/" + getResources().getStringArray(R.array.wallpapers)[position]
                + ".png";
        String toastText;
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IOException("Failed to make folder " + folder);
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bi.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (new File(file).exists()) {
            toastText = "Wallpaper saved to " + file;
        } else {
            toastText = "Failed to save wallpaper.";
        }
        makeToast(toastText);

    }

    private int[] getWallpaperSize() {
        int[] size = new int[2];
        Resources res = getResources();
        if (res.getBoolean(R.bool.is_nodpi)) {
            size[0] = 960;
            size[1] = 800;
        } else if (res.getBoolean(R.bool.is_sw600dp_nodpi)) {
            size[0] = 1600;
            size[1] = 1280;
        } else if (res.getBoolean(R.bool.is_sw720dp_nodpi)) {
            size[0] = 1920;
            size[1] = 1280;
        } else if (res.getBoolean(R.bool.is_xhdpi)) {
            size[0] = 1340;
            size[1] = 1196;
        }
        return size;
    }

    private void findWallpapers() {
        mThumbs = new ArrayList<Drawable>();
        mImages = new ArrayList<Integer>();

        final Resources resources = getResources();
        final String packageName = getApplication().getPackageName();

        addWallpapers(resources, packageName, R.array.wallpapers);
        addWallpapers(resources, packageName, R.array.extra_wallpapers);
    }

    private void addWallpapers(Resources resources, String packageName, int list) {
        final String[] extras = resources.getStringArray(list);
        Drawable d = ImageHelper.resize(getApplicationContext(),
                getResources().getDrawable(R.drawable.app_icon), 100);
        for (String extra : extras) {
            int res = resources.getIdentifier(extra, "drawable", packageName);
            if (res != 0) {
                mImages.add(res);
                mThumbs.add(d);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsWallpaperSet = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            mLoader.cancel(true);
            mLoader = null;
        }
    }

    /*
     * When using touch if you tap an image it triggers both the onItemClick and
     * the onTouchEvent causing the wallpaper to be set twice. Ensure we only
     * set the wallpaper once.
     */
    private void selectWallpaper(int position) {
        if (mIsWallpaperSet) {
            return;
        }

        mIsWallpaperSet = true;
        try {
            Bitmap b = BitmapFactory.decodeResource(getResources(), mImages.get(position));
            int[] size = getWallpaperSize();
            Bitmap bi = ImageHelper.resize(getApplicationContext(), b, size[0], size[1]);
            WallpaperManager wm = WallpaperManager.getInstance(getApplicationContext());
            wm.setBitmap(bi);
            setResult(RESULT_OK);
            finish();
        } catch (IOException e) {
            Log.e("Paperless System", "Failed to set wallpaper: " + e);
        }
    }

    class ImageResizer extends AsyncTask<Integer, Integer, ArrayList<Drawable>> {

        private ArrayList<Drawable> array;

        protected ArrayList<Drawable> doInBackground(Integer... i) {
            if (isCancelled()) return null;
            final String[] extras = getResources().getStringArray(i[0]);
            array = new ArrayList<Drawable>();
            for (int in = 0; in < extras.length; in++) {
                int res = getResources().getIdentifier(extras[in], "drawable",
                        getApplication().getPackageName());
                if (res != 0) {
                    array.add(ImageHelper.resize(getApplicationContext(), getResources()
                            .getDrawable(res), 75));
                }
                publishProgress(in);
            }
            return array;
        }

        @Override
        protected void onProgressUpdate(Integer... i) {
            mLayout.add(array.get(i[0]), i[0]);
        }
    }

    class WallpaperLoader extends AsyncTask<Integer, Void, Bitmap> {

        BitmapFactory.Options mOptions;

        WallpaperLoader() {
            mOptions = new BitmapFactory.Options();
            mOptions.inDither = false;
            mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }

        protected Bitmap doInBackground(Integer... params) {
            if (isCancelled()) return null;
            Bitmap b = BitmapFactory.decodeResource(getResources(), mImages.get(params[0]));
            int[] size = getWallpaperSize();
            return ImageHelper.resize(getApplicationContext(), b, size[0], size[1]);
        }

        @Override
        protected void onPostExecute(Bitmap b) {
            if (b == null) return;

            if (!isCancelled() && !mOptions.mCancel) {
                // Help the GC
                if (mBitmap != null) {
                    mBitmap.recycle();
                }

                setTitle(getResources().getStringArray(
                        R.array.wallpapers)[mLayout.getCurrent()].toUpperCase());

                final ImageView view = mImageView;
                view.setImageBitmap(b);

                mBitmap = b;

                view.postInvalidate();

                mLoader = null;
            } else {
                b.recycle();
            }
        }

        void cancel() {
            mOptions.requestCancelDecode();
            super.cancel(true);
        }
    }

    private int getLastWallpaper() {
        SharedPreferences pref = getSharedPreferences(SETTINGS, 0);
        return pref.getInt(LAST_WALLPAPER, 0);
    }

    private void setLastWallpaper(int i) {
        SharedPreferences.Editor editor = getSharedPreferences(SETTINGS, 0).edit();
        editor.putInt(LAST_WALLPAPER, i);
        editor.commit();
    }
}
