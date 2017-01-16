/*
* Copyright (C) 2016 SlimRoms Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.slim.launcher.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Workspace;
import org.slim.launcher.settings.SettingsActivity;
import org.slim.launcher.settings.SettingsProvider;

import org.slim.launcher.SlimLauncher;

import java.net.URISyntaxException;
import java.util.List;

public class GestureHelper {

    public static final String TAG = "GestureHelper";

    public static final int MIN_UP_DOWN_GESTURE_DISTANCE = 200;

    public static final String ACTION_DEFAULT_HOMESCREEN = "default_homescreen";
    public static final String ACTION_OPEN_APP_DRAWER = "open_app_drawer";
    public static final String ACTION_OVERVIEW_MODE = "show_previews";
    public static final String ACTION_LAUNCHER_SETTINGS = "show_settings";
    public static final String ACTION_LAST_APP = "last_app";
    public static final String ACTION_CUSTOM = "custom";

    // gesture constants
    private static final String DOUBLE_TAP_GESTURE = "double_tap";
    private static final String SPREAD_GESTURE = "spread";
    private static final String PINCH_GESTURE = "pinch";

    // gestures
    private String mLeftUpGestureAction;
    private String mMiddleUpGestureAction;
    private String mRightUpGestureAction;
    private String mLeftDownGestureAction;
    private String mMiddleDownGestureAction;
    private String mRightDownGestureAction;
    private String mPinchGestureAction;
    private String mSpreadGestureAction;
    private String mDoubleTapGestureAction;

    private SlimLauncher mLauncher;
    private int mSector;

    public GestureHelper(SlimLauncher launcher) {
        mLauncher = launcher;
        init(launcher);
        updateGestures();
    }

    void init(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        mSector = width / 3;
    }

    public void updateGestures() {
        String gesture_def = mLauncher.getString(R.string.gesture_default);
        mLeftUpGestureAction = SettingsProvider.getString(mLauncher,
                SettingsProvider.LEFT_UP_GESTURE_ACTION, gesture_def);
        mMiddleUpGestureAction = SettingsProvider.getString(mLauncher,
                SettingsProvider.MIDDLE_UP_GESTURE_ACTION, gesture_def);
        mRightUpGestureAction = SettingsProvider.getString(mLauncher,
                SettingsProvider.RIGHT_UP_GESTURE_ACTION, gesture_def);
        mLeftDownGestureAction = SettingsProvider.getString(mLauncher,
                SettingsProvider.LEFT_DOWN_GESTURE_ACTION, gesture_def);
        mMiddleDownGestureAction = SettingsProvider.getString(mLauncher,
                SettingsProvider.MIDDLE_DOWN_GESTURE_ACTION, gesture_def);
        mRightDownGestureAction = SettingsProvider.getString(mLauncher,
                SettingsProvider.RIGHT_DOWN_GESTURE_ACTION, gesture_def);

        mPinchGestureAction = SettingsProvider.getString(mLauncher,
                SettingsProvider.PINCH_GESTURE_ACTION, gesture_def);
        mSpreadGestureAction = SettingsProvider.getString(mLauncher,
                SettingsProvider.SPREAD_GESTURE_ACTION, gesture_def);
        mDoubleTapGestureAction = SettingsProvider.getString(mLauncher,
                SettingsProvider.DOUBLE_TAP_GESTURE_ACTION, gesture_def);
    }

    public void handleGestureAction(Gesture gesture) {
        switch (gesture) {
            case DOUBLE_TAP:
                performGestureAction(mDoubleTapGestureAction, DOUBLE_TAP_GESTURE);
                break;
            case SPREAD:
                performGestureAction(mSpreadGestureAction, SPREAD_GESTURE);
                break;
            case PINCH:
                performGestureAction(mPinchGestureAction, PINCH_GESTURE);
        }
    }

    private void performGestureAction(String gestureAction, String gesture) {
        Workspace workspace = mLauncher.getWorkspace();
        switch (gestureAction) {
            case  ACTION_DEFAULT_HOMESCREEN:
                workspace.setCurrentPage(workspace.getPageIndexForScreenId(-1));
                break;
            case ACTION_OPEN_APP_DRAWER:
                mLauncher.showAppsView(false /* animated */,
                        true /* updatePredictedApps */, false /* focusSearchBar */);
                break;
            case ACTION_OVERVIEW_MODE:
                mLauncher.showOverviewMode(true);
                break;
            case ACTION_LAUNCHER_SETTINGS:
                Intent preferences = new Intent().setClass(mLauncher, SettingsActivity.class);
                preferences.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mLauncher.startActivity(preferences);
                break;
            case ACTION_LAST_APP:
                getLastApp(mLauncher);
                break;
            case ACTION_CUSTOM:
                String uri = SettingsProvider.getString(mLauncher,
                        gesture + "_gesture_action_custom", "");
                if (!uri.equals("")) {
                    try {
                        Intent intent = Intent.parseUri(uri, 0);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mLauncher.startActivity(intent);
                    } catch (URISyntaxException e) {
                        Log.e(TAG, "Unable to start gesture action " + gestureAction, e);
                    }
                }
                break;
        }
    }

    public boolean handleFling(MotionEvent start, MotionEvent finish) {
        switch(identifyGesture(finish.getRawX(), finish.getRawY(),
                start.getRawX(), start.getRawY())) {
            case UP_LEFT:
                performGestureAction(mLeftUpGestureAction, "left_up");
                break;
            case UP_MIDDLE:
                performGestureAction(mMiddleUpGestureAction, "middle_up");
                break;
            case UP_RIGHT:
                performGestureAction(mRightUpGestureAction, "right_up");
                break;
            case DOWN_LEFT:
                performGestureAction(mLeftDownGestureAction, "left_down");
                break;
            case DOWN_MIDDLE:
                performGestureAction(mMiddleDownGestureAction, "middle_down");
                break;
            case DOWN_RIGHT:
                performGestureAction(mRightDownGestureAction, "right_down");
                break;
        }
        return false;
    }

    private void getLastApp(Launcher launcher) {
        String packageName;
        String intentPackage = null;
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        final ActivityManager am = (ActivityManager)
                launcher.getSystemService(Activity.ACTIVITY_SERVICE);
        String defaultHomePackage = "com.android.launcher";
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = launcher.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo != null && !res.activityInfo.packageName.equals("android")) {
            defaultHomePackage = res.activityInfo.packageName;
        }
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(5);
        // lets get enough tasks to find something to switch to
        // Note, we'll only get as many as the system currently has - up to 5
        for (int i = tasks.size() - 1; i > 0; i--) {
            packageName = tasks.get(i).topActivity.getPackageName();
            if (!packageName.equals(defaultHomePackage)
                    && !packageName.equals("com.android.systemui")
                    && !packageName.equals(launcher.getPackageName())) {
                intentPackage = packageName;
            }
        }
        if (intentPackage != null) {
            Intent intent1 = launcher.getPackageManager().getLaunchIntentForPackage(intentPackage);
            launcher.startActivity(intent1);
        } else {
            Toast.makeText(launcher, launcher.getString(R.string.no_last_app), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @SuppressWarnings("unused")
    public Gesture identifyGesture(float upX, float upY, float downX, float downY) {

        if (isSwipeDOWN(upY, downY)) {

            if (isSwipeLEFT(downX)) {
                return Gesture.DOWN_LEFT;
            } else if (isSwipeRIGHT(downX)) {
                return Gesture.DOWN_RIGHT;
            } else if (isSwipeMIDDLE(downX)) {
                return Gesture.DOWN_MIDDLE;
            }
        } else if (isSwipeUP(upY, downY)) {
            if (isSwipeLEFT(downX)) {
                return Gesture.UP_LEFT;
            } else if (isSwipeRIGHT(downX)) {
                return Gesture.UP_RIGHT;
            } else if (isSwipeMIDDLE(downX)) {
                return Gesture.UP_MIDDLE;
            }
        }

        return Gesture.NONE;
    }

    @SuppressWarnings("unused")
    public static boolean doesSwipeDownContainShowAllApps(Context context) {
        return (SettingsProvider.getString(context,
                SettingsProvider.LEFT_DOWN_GESTURE_ACTION, "").equals(ACTION_OPEN_APP_DRAWER)
                || SettingsProvider.getString(context,
                SettingsProvider.MIDDLE_DOWN_GESTURE_ACTION, "").equals(ACTION_OPEN_APP_DRAWER)
                || SettingsProvider.getString(context,
                SettingsProvider.RIGHT_DOWN_GESTURE_ACTION, "").equals(ACTION_OPEN_APP_DRAWER));
    }

    @SuppressWarnings("unused")
    public static boolean doesSwipeUpContainShowAllApps(Context context) {
        return (SettingsProvider.getString(context,
                SettingsProvider.LEFT_UP_GESTURE_ACTION, "").equals(ACTION_OPEN_APP_DRAWER)
                || SettingsProvider.getString(context,
                SettingsProvider.MIDDLE_UP_GESTURE_ACTION, "").equals(ACTION_OPEN_APP_DRAWER)
                || SettingsProvider.getString(context,
                SettingsProvider.RIGHT_UP_GESTURE_ACTION, "").equals(ACTION_OPEN_APP_DRAWER));
    }

    public boolean isSwipeDOWN(float upY, float downY) {
        return (upY - downY) > MIN_UP_DOWN_GESTURE_DISTANCE;
    }

    public boolean isSwipeUP(float upY, float downY) {
        return ((upY - downY) < -MIN_UP_DOWN_GESTURE_DISTANCE);
    }

    public boolean isSwipeLEFT(float downX) {
        return downX < mSector;
    }

    public boolean isSwipeMIDDLE(float downX) {
        return downX > mSector && downX < (mSector * 2);
    }

    public boolean isSwipeRIGHT(float downX) {
        return downX > (mSector * 2);
    }

    public enum Gesture {
        DOWN_LEFT,
        DOWN_MIDDLE,
        DOWN_RIGHT,
        UP_LEFT,
        UP_MIDDLE,
        UP_RIGHT,
        DOUBLE_TAP,
        PINCH,
        SPREAD,
        NONE
    }
}
