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

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class SlimWallpaper extends Activity implements AdapterView.OnItemSelectedListener {

    private Gallery mGallery;
    private ImageView mImageView;
    private TextView mInfoView;
    private boolean mIsWallpaperSet;

    private Bitmap mBitmap;

    private ArrayList<Drawable> mThumbs;
    private ArrayList<Integer> mImages;
    private WallpaperLoader mLoader;
    private ImageAdapter mAdapter;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        findWallpapers();

        setContentView(R.layout.wallpaper_chooser);

        mAdapter = new ImageAdapter(SlimWallpaper.this);

        mGallery = (Gallery) findViewById(R.id.gallery);
        mGallery.setAdapter(mAdapter);
        mGallery.setOnItemSelectedListener(this);
        mGallery.setCallbackDuringFling(false);

        mImageView = (ImageView) findViewById(R.id.wallpaper);
        mInfoView = (TextView) findViewById(R.id.info);

        new ImageResizer().execute(R.array.wallpapers);
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
                selectWallpaper(mGallery.getSelectedItemPosition());
                break;

            case R.id.save:
                saveWallpaper(mGallery.getSelectedItemPosition());
                break;
        }
        return true;
    }

    private void saveWallpaper(int position) {
        Bitmap b = BitmapFactory.decodeResource(getResources(), mImages.get(position));
        File folder = new File(Environment.getExternalStorageDirectory() + "/Slim/wallpapers");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String file = folder + "/" + getResources().getStringArray(R.array.wallpapers)[position] + ".png";
        try {
            FileOutputStream out = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (new File(file).exists()) {
            Toast.makeText(getApplicationContext(), "Wallpaper saved to " + file, Toast.LENGTH_SHORT).show();
        }

    }

    private void findWallpapers() {
        mThumbs = new ArrayList<Drawable>(24);
        mImages = new ArrayList<Integer>(24);

        final Resources resources = getResources();
        final String packageName = getApplication().getPackageName();

        addWallpapers(resources, packageName, R.array.wallpapers);
    }

    private void addWallpapers(Resources resources, String packageName, int list) {
        final String[] extras = resources.getStringArray(list);
        for (String extra : extras) {
            int res = resources.getIdentifier(extra, "drawable", packageName);
            Drawable d = ImageHelper.resize(getApplicationContext(),
                    getResources().getDrawable(R.drawable.app_icon), 100);
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

    public void onItemSelected(AdapterView parent, View v, int position, long id) {
        if (mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            mLoader.cancel();
        }
        mLoader = (WallpaperLoader) new WallpaperLoader().execute(position);
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
            InputStream stream = getResources().openRawResource(mImages.get(position));
            setWallpaper(stream);
            setResult(RESULT_OK);
            finish();
        } catch (IOException e) {
            Log.e("Paperless System", "Failed to set wallpaper: " + e);
        }
    }

    public void onNothingSelected(AdapterView parent) {
    }

    private class ImageAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        ImageAdapter(SlimWallpaper context) {
            mLayoutInflater = context.getLayoutInflater();
        }

        public int getCount() {
            return mThumbs.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView image;

            if (convertView == null) {
                image = (ImageView) mLayoutInflater.inflate(R.layout.wallpaper_item, parent, false);
            } else {
                image = (ImageView) convertView;
            }

            Drawable thumbDrawable = mThumbs.get(position);
            if (thumbDrawable != null) {
                thumbDrawable.setDither(true);
            }
            image.setImageDrawable(thumbDrawable);
            return image;
        }
    }

    class ImageResizer extends AsyncTask<Integer, Void, ArrayList<Drawable>> {
        ImageResizer() {

        }

        protected ArrayList<Drawable> doInBackground(Integer... i) {
            if (isCancelled()) return null;
            final String[] extras = getResources().getStringArray(i[0]);
            final ArrayList<Drawable> returnValue = new ArrayList<Drawable>();
            for (String extra : extras) {
                int res = getResources().getIdentifier(extra, "drawable", getApplication().getPackageName());
                if (res != 0) {
                    returnValue.add(ImageHelper.resize(getApplicationContext(), getResources().getDrawable(res), 100));
                }
            }
            return returnValue;
        }

        @Override
        protected void onPostExecute(ArrayList<Drawable> arrayList) {
            for (int i = 0; i < mThumbs.size(); i++) {
                mThumbs.set(i, arrayList.get(i));
                mAdapter.notifyDataSetChanged();
            }
        }

        void cancel() {

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
            try {
                return BitmapFactory.decodeResource(getResources(),
                        mImages.get(params[0]), mOptions);
            } catch (OutOfMemoryError e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap b) {
            if (b == null) return;

            if (!isCancelled() && !mOptions.mCancel) {
                // Help the GC
                if (mBitmap != null) {
                    mBitmap.recycle();
                }

                mInfoView.setText(getResources().getStringArray(
                        R.array.wallpapers)[mGallery.getSelectedItemPosition()]);

                final ImageView view = mImageView;
                view.setImageBitmap(b);

                mBitmap = b;

                final Drawable drawable = view.getDrawable();
                drawable.setFilterBitmap(true);
                drawable.setDither(true);

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
}
