package org.slim.launcher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.android.launcher3.PageIndicator;
import com.android.launcher3.PageIndicatorMarker;
import com.android.launcher3.R;

import java.util.ArrayList;

/**
 * Created by gmillz on 11/6/16.
 */
public class SlimPageIndicator extends PageIndicator {

    private static final int FADE_FRAME_MS = 80;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mCount = 0;
    private int mCurrentPage;
    float mPositionOffset;
    int mScrollState;

    int mFadeDelay = 700;
    int mFadeBy = 0xFF / (mFadeDelay / FADE_FRAME_MS);
    boolean mFades = true;

    private final Runnable mFadeRunnable = new Runnable() {
        @Override public void run() {
            if (!mFades) return;

            final int alpha = Math.max(mPaint.getAlpha() - mFadeBy, 0);
            mPaint.setAlpha(alpha);
            invalidate();
            if (alpha > 0) {
                postDelayed(this, FADE_FRAME_MS);
            }
        }
    };

    public SlimPageIndicator(Context context) {
        this(context, null);
    }

    public SlimPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        mPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCount == 0) {
            return;
        }

        if (mCurrentPage >= mCount) {
            return;
        }

        final int paddingLeft = getPaddingLeft();
        final int width = getResources().getDisplayMetrics().widthPixels;
        final float pageWidth = (width - paddingLeft - getPaddingRight()) / (1f * mCount);
        final float left = paddingLeft + pageWidth * (mCurrentPage + mPositionOffset);
        final float right = left + pageWidth;
        final float top = getPaddingTop();
        final float bottom = getHeight() - getPaddingBottom();
        canvas.drawRect(left, top, right, bottom, mPaint);
    }

    @Override
    protected void addMarker(int index, PageMarkerResources marker, boolean allowAnimations) {
        mCount++;
    }

    @Override
    protected void updateMarker(int index, PageMarkerResources marker) {
    }

    @Override
    protected void addMarkers(ArrayList<PageMarkerResources> markers, boolean allowAnimations) {
        mCount += markers.size();
    }

    @Override
    protected void removeMarker(int index, boolean allowAnimations) {
        mCount--;
        invalidate();
    }

    @Override
    protected void removeAllMarkers(boolean allowAnimations) {
        mCount = 0;
        invalidate();
    }

    @Override
    public void setActiveMarker(int index) {
        Log.d("TEST", "setActiveMarker");
        removeCallbacks(mFadeRunnable);
        mPaint.setAlpha(0xFF);
        mCurrentPage = index;
        invalidate();
        postDelayed(mFadeRunnable, mFadeDelay);
    }
}
