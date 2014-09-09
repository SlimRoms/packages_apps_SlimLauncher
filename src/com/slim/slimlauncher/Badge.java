package com.slim.slimlauncher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class Badge {

    private int mCount;
    private TextView mBadgeCount;
    private Context mContext;

    public static int mLeftRightPadding;
    public static int mTopBottomPadding;

    public Badge(Context context, int count) {
        mCount = count;
        mContext = context;
    }

    public Drawable textToDrawable() {
        setupTextView();

        Bitmap badge = Bitmap.createBitmap(mBadgeCount.getMeasuredWidth(),
                mBadgeCount.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(badge);

        mBadgeCount.layout(0, 0, mBadgeCount.getMeasuredWidth(), mBadgeCount.getMeasuredHeight());
        mBadgeCount.draw(c);

        return new BitmapDrawable(mContext.getResources(), badge);
    }

    private void initBadge() {

        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();

        mLeftRightPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, dm));
        mTopBottomPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, dm));

        mBadgeCount = new TextView(mContext);
        mBadgeCount.setTextColor(0xFF000000);
        mBadgeCount.setGravity(Gravity.CENTER);
        mBadgeCount.setIncludeFontPadding(false);
        mBadgeCount.setPadding(mLeftRightPadding, mTopBottomPadding,
                mLeftRightPadding, mTopBottomPadding);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(0xFFFFFFFF);
        gradientDrawable.setCornerRadius(50);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);

        mBadgeCount.setBackground(gradientDrawable);
        mBadgeCount.setDrawingCacheEnabled(true);
    }

    protected TextView setupTextView() {
        initBadge();

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);
        int displayWidth = size.x;
        int displayHeight = size.y;

        int measuredWidth = View.MeasureSpec.makeMeasureSpec(displayWidth, View.MeasureSpec.AT_MOST);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        mBadgeCount.setText(Integer.toString(mCount));
        mBadgeCount.measure(measuredWidth, measuredHeight);

        return mBadgeCount;
    }

    public Drawable getDrawable() {
        return textToDrawable();
    }
}
