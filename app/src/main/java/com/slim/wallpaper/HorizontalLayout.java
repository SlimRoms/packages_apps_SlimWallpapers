package com.slim.wallpaper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HorizontalLayout extends LinearLayout {

    Context mContext;
    OnImageClickListener mOnImageClickListener;

    int mCurrent = 0;

    public HorizontalLayout(Context context) {
        super(context);

        mContext = context;
    }

    public HorizontalLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mContext = context;
    }

    public HorizontalLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);

        mContext = context;
    }

    void add(Drawable d, int i) {
        if (getChildAt(i) != null) {
            removeViewAt(i);
        }
        addView(getImageButton(d, i), i);
    }

    int getCurrent() {
        return mCurrent;
    }

    void setDefault(int i) {
       setCurrent(i);
    }

    void setCurrent(int i) {
        mCurrent = i;
    }

    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        mOnImageClickListener = onImageClickListener;
    }

    ImageButton getImageButton(Drawable d, int i) {

        ImageButton imageButton = new ImageButton(mContext);
        imageButton.setLayoutParams(new ViewGroup.LayoutParams(220, 220));
        imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageButton.setId(i);
        imageButton.setImageDrawable(d);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                setCurrent(i);
                mOnImageClickListener.onImageClick(v);
            }
        });

        return imageButton;
    }

    public interface OnImageClickListener {
        public void onImageClick(View v);
    }
}
