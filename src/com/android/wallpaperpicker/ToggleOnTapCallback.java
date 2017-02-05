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

package com.android.wallpaperpicker;

import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Callback that toggles the visibility of the target view when crop view is tapped.
 */
class ToggleOnTapCallback implements CropView.TouchCallback {

    private final View mViewtoToggle;

    private ViewPropertyAnimator mAnim;
    private boolean mIgnoreNextTap;

    ToggleOnTapCallback(View viewtoHide) {
        mViewtoToggle = viewtoHide;
    }

    @Override
    public void onTouchDown() {
        if (mAnim != null) {
            mAnim.cancel();
        }
        if (mViewtoToggle.getAlpha() == 1f) {
            mIgnoreNextTap = true;
        }

        mAnim = mViewtoToggle.animate();
        mAnim.alpha(0f)
            .setDuration(150)
            .withEndAction(new Runnable() {
                public void run() {
                    mViewtoToggle.setVisibility(View.INVISIBLE);
                }
            });

        mAnim.setInterpolator(new AccelerateInterpolator(0.75f));
        mAnim.start();
    }

    @Override
    public void onTouchUp() {
        mIgnoreNextTap = false;
    }

    @Override
    public void onTap() {
        boolean ignoreTap = mIgnoreNextTap;
        mIgnoreNextTap = false;
        if (!ignoreTap) {
            if (mAnim != null) {
                mAnim.cancel();
            }
            mViewtoToggle.setVisibility(View.VISIBLE);
            mAnim = mViewtoToggle.animate();
            mAnim.alpha(1f)
                 .setDuration(150)
                 .setInterpolator(new DecelerateInterpolator(0.75f));
            mAnim.start();
        }
    }
}
