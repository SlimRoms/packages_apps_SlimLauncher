package org.slim.launcher.util;

import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import com.android.launcher3.Hotseat;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.allapps.AllAppsContainerView;

import org.slim.launcher.SlimLauncher;
import org.slim.launcher.settings.SettingsProvider;

public class AllAppsAnimation {

    private static final int ANIMATION_OFFSET = 100;

    private AllAppsContainerView mAppsView;
    private Hotseat mHotseat;
    private DisplayMetrics mMetrics;
    private DisplayMetrics mRealMetrics;
    private int mHotseatBarHeight;

    private final Handler mHandler;

    private VelocityTracker mVelocityTracker;
    private float mMaximumFlingVelocity;
    private float mMinimumFlingVelocity;

    private float mOffset;
    private float mDownY;
    private float mDownX;

    private int mHotseatColor;
    private int mAppsColor;

    private Runnable mUpRunnable = new Runnable() {
        @Override
        public void run() {
            update(mOffset);
            mOffset -= ANIMATION_OFFSET * mMetrics.density;
            finishUpAnimation(mOffset);
        }
    };

    private Runnable mDownRunnable = new Runnable() {
        @Override
        public void run() {
            update(mOffset);
            mOffset += ANIMATION_OFFSET * mMetrics.density;
            finishDownAnimation(mOffset);
        }
    };

    public AllAppsAnimation(SlimLauncher launcher, AllAppsContainerView appsView) {
        mAppsView = appsView;
        mHotseat = launcher.getHotseat();
        mMetrics = new DisplayMetrics();
        mRealMetrics = new DisplayMetrics();
        mHotseatBarHeight = LauncherAppState.getInstance()
                .getInvariantDeviceProfile().portraitProfile.hotseatBarHeightPx;
        mHandler = new Handler();

        mAppsView.getAppsRecyclerView().addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                //return SlimLauncher.getInstance().allAppsEvent(e);
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        mAppsView.setVisibility(View.VISIBLE);
        mAppsView.getContentView().setVisibility(View.VISIBLE);

        Display display = launcher.getWindowManager().getDefaultDisplay();
        display.getRealMetrics(mRealMetrics);
        display.getMetrics(mMetrics);

        mAppsView.setY(mMetrics.heightPixels);

        ViewConfiguration viewConfig = ViewConfiguration.get(launcher);
        mMaximumFlingVelocity = viewConfig.getScaledMaximumFlingVelocity();
        mMinimumFlingVelocity = viewConfig.getScaledMinimumFlingVelocity();

        updateBackgroundColor();
    }

    public void updateBackgroundColor() {
        mHotseatColor = SettingsProvider.getInt(mAppsView.getContext(),
                SettingsProvider.KEY_HOTSEAT_BACKGROUND, Color.TRANSPARENT);

        mHotseat.setBackgroundColor(mHotseatColor);
        mAppsView.setBackgroundColor(mHotseatColor);

        mAppsColor = SettingsProvider.getInt(mAppsView.getContext(),
                SettingsProvider.KEY_DRAWER_BACKGROUND_COLOR,
                ContextCompat.getColor(mAppsView.getContext(), R.color.quantum_panel_bg_color));
    }

    private int getAnimatedColor(float delta) {
        if (delta < 0.0f) {
            delta = 0.0f;
        }
        float iDelta = 1f - delta;
        final int a = (int) (Color.alpha(mAppsColor) * delta + Color.alpha(mHotseatColor) * iDelta);
        final int r = (int) (Color.red(mAppsColor) * delta + Color.red(mHotseatColor) * iDelta);
        final int g = (int) (Color.green(mAppsColor) * delta + Color.green(mHotseatColor) * iDelta);
        final int b = (int) (Color.blue(mAppsColor) * delta + Color.blue(mHotseatColor) * iDelta);
        return Color.argb(a, r, g, b);
    }

    private boolean atTopOfApps() {
        int pos = ((LinearLayoutManager) mAppsView.getAppsRecyclerView().getLayoutManager())
                .findFirstCompletelyVisibleItemPosition();
        return pos == 1;
    }

    public boolean appDrawerTouchEvent(MotionEvent event) {
        updateVelocityTracker(event);
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (atTopOfApps() && GestureHelper.isSwipeDOWN(event.getRawY(), mDownY)) {
                finishDownAnimation(event.getRawY() - mDownY);
            }
            mDownY = 0;
            mDownX = 0;
            clearVelocityTracker();
        } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mDownY = event.getRawY();
            mDownX = event.getRawX();
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if ((atTopOfApps() && GestureHelper.isSwipeDOWN(event.getRawY(), mDownY))
                    || (-2 < mAppsView.getY()) || (mAppsView.getY() > 2)) {
                if (!atTopOfApps()) return false;
                if (isMovement(event)) {
                    mAppsView.setVisibility(View.VISIBLE);
                    mAppsView.getContentView().setVisibility(View.VISIBLE);
                    update(event.getRawY() - mDownY);
                }
            }
        }
        return atTopOfApps() && GestureHelper.isSwipeDOWN(event.getRawY(), mDownY);
    }

    public boolean hotseatTouchEvent(MotionEvent event) {
        updateVelocityTracker(event);
        if (event.getActionMasked() ==MotionEvent.ACTION_UP) {
            if (isFling(event) || event.getRawY() < mMetrics.heightPixels / 2) {
                finishUpAnimation((event.getRawY() - mDownY) + mMetrics.heightPixels);
            } else {
                finishDownAnimation(event.getRawY() - mDownY + mMetrics.heightPixels);
            }
            mDownY = 0;
            mDownX = 0;
            clearVelocityTracker();
            return false;
        } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mDownX = event.getRawX();
            mDownY = event.getRawY();
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if (isMovement(event)) {
                mAppsView.setVisibility(View.VISIBLE);
                mAppsView.getContentView().setVisibility(View.VISIBLE);
                update((event.getRawY() - mDownY) + mMetrics.heightPixels);
                return true;
            }
        }
        return false;
    }

    private boolean isMovement(MotionEvent event) {
        int touchSlop = ViewConfiguration.get(mAppsView.getContext()).getScaledTouchSlop();
        return Math.abs(mDownX - event.getRawX()) > touchSlop ||
                Math.abs(mDownY - event.getRawY()) > touchSlop;
    }

    private boolean isFling(MotionEvent e) {
        if (mVelocityTracker == null) return false;
        final VelocityTracker velocityTracker = mVelocityTracker;
        final int pointerId = e.getPointerId(0);
        velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
        final float velocityY = velocityTracker.getYVelocity(pointerId);
        return Math.abs(velocityY) > mMinimumFlingVelocity;
    }

    private void updateVelocityTracker(MotionEvent e) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(e);
    }

    private void clearVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void update(float y) {
        if (y < 0) {
            y = 0;
        }
        handleStatusBarColor(y);
        mAppsView.getContentView().setAlpha(getAppsAlpha(y));
        mAppsView.setY(y);
        mHotseat.getContent().setAlpha(getHotseatAlpha(y));
        mHotseat.setY(y - mHotseatBarHeight);
        int b = getAnimatedColor(getAppsAlpha(y - 0.1f));
        mAppsView.setBackgroundColor(b);
        mHotseat.setBackgroundColor(b);

    }

    private void handleStatusBarColor(float y) {
        Log.d("TEST", "y=" + y + " : statusbar=" + getStatusBarHeight());
        if ((y - mHotseatBarHeight) > getStatusBarHeight()) {
            mAppsView.getContentView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } else if ((y - mHotseatBarHeight) < getStatusBarHeight()) {
            boolean useCard = SettingsProvider.getBoolean(mAppsView.getContext(),
                    SettingsProvider.KEY_DRAWER_DISABLE_CARD, false);
            int color = SettingsProvider.getInt(mAppsView.getContext(),
                    SettingsProvider.KEY_DRAWER_BACKGROUND_COLOR,
                    ContextCompat.getColor(mAppsView.getContext(), R.color.quantum_panel_bg_color));
            boolean lightStatusBar = ColorUtils.darkTextColor(color);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (lightStatusBar && !useCard) {
                    mAppsView.getContentView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    mAppsView.getContentView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                }
            }
        }
    }

    public void showAllApps() {
        finishUpAnimation(mAppsView.getY());
    }

    public void hideAllApps() {
        finishDownAnimation(mAppsView.getY());
    }

    private void finishUpAnimation(float offset) {
        if (offset <= 0) {
            update(0);
            mOffset = -1;
            return;
        }
        mOffset = offset;
        mHandler.removeCallbacks(mDownRunnable);
        mHandler.removeCallbacks(mUpRunnable);
        mHandler.post(mUpRunnable);
    }

    private void finishDownAnimation(float offset) {
        if (offset >= mMetrics.heightPixels) {
            update(mMetrics.heightPixels);
            mOffset = -1;
            return;
        }
        handleStatusBarColor(offset);
        mOffset = offset;
        mHandler.removeCallbacks(mUpRunnable);
        mHandler.removeCallbacks(mDownRunnable);
        mHandler.post(mDownRunnable);
    }

    private float getAppsAlpha(float y) {
        int height = mMetrics.heightPixels;
        return (height - y) / height;
    }

    private float getHotseatAlpha(float y) {
        int height = mMetrics.heightPixels;
        return y / height;
    }

    public int getStatusBarHeight() {
        return (int) (24 * mMetrics.density);
    }
}
