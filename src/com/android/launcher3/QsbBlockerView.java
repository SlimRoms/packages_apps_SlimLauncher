/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.launcher3;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.launcher3.Workspace.State;

import org.slim.launcher.pixel.OnWeatherInfoListener;
import org.slim.launcher.pixel.ShadowHostView;
import org.slim.launcher.pixel.WeatherInfo;
import org.slim.launcher.pixel.WeatherThing;

/**
 * A simple view used to show the region blocked by QSB during drag and drop.
 */
public class QsbBlockerView extends FrameLayout implements Workspace.OnStateChangeListener, OnWeatherInfoListener {

    private static final int VISIBLE_ALPHA = 100;

    private View mView;
    private int mState = 0;
    private static final boolean DEBUG = false;

    private final Paint mBgPaint;

    public QsbBlockerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(Color.WHITE);
        mBgPaint.setAlpha(0);
    }

    private static int getAlphaForState(State state) {
        switch (state) {
            case SPRING_LOADED:
            case OVERVIEW:
            case OVERVIEW_HIDDEN:
                return VISIBLE_ALPHA;
        }
        return 0;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        Workspace w = Launcher.getLauncher(getContext()).getWorkspace();
        w.setOnStateChangeListener(this);
        prepareStateChange(w.getState(), null);

        WeatherInfo weatherInfo =
                WeatherThing.getInstance(getContext()).getWeatherInfoAndAddListener(this);
        if (weatherInfo != null) {
            onWeatherInfo(weatherInfo);
        }
    }

    @Override
    public void prepareStateChange(State toState, AnimatorSet targetAnim) {
        int finalAlpha = getAlphaForState(toState);
        if (targetAnim == null) {
            mBgPaint.setAlpha(finalAlpha);
            invalidate();
        } else {
            ObjectAnimator anim = ObjectAnimator.ofArgb(mBgPaint, "alpha", finalAlpha);
            anim.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            targetAnim.play(anim);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(mBgPaint);
    }

    @Override
    public void onWeatherInfo(WeatherInfo weatherInfo) {
        View view = mView;
        int i = mState;
        mView = ShadowHostView.bG(weatherInfo, this, mView);
        mState = 2;
        if (mView == null) {
            View inflate;
            mState = 1;
            if (view == null || i != 1) {
                inflate = LayoutInflater.from(getContext()).inflate(R.layout.date_widget, this, false);
            } else {
                inflate = view;
            }
            mView = inflate;
        }
        if (i != mState) {
            if (view != null) {
                view.animate().setDuration(200).alpha(0.0f).withEndAction(new QsbBlockerViewViewRemover(this, view));
            }
            addView(mView);
            mView.setAlpha(0.0f);
            mView.animate().setDuration(200).alpha(1.0f);
        } else if (view != mView) {
            if (view != null) {
                removeView(view);
            }
            addView(mView);
        }
    }

    private final class QsbBlockerViewViewRemover implements Runnable {
        final QsbBlockerView mQsbBlockerView;
        final View mView;

        QsbBlockerViewViewRemover(QsbBlockerView qsbBlockerView, View view) {
            mQsbBlockerView = qsbBlockerView;
            mView = view;
        }

        @Override
        public void run() {
            mQsbBlockerView.removeView(mView);
        }
    }
}
