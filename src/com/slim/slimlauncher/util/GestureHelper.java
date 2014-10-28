/*
* Copyright (C) 2014 SlimRoms Project
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
package com.slim.slimlauncher.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.slim.slimlauncher.AppsCustomizePagedView;
import com.slim.slimlauncher.Launcher;
import com.slim.slimlauncher.R;
import com.slim.slimlauncher.Workspace;
import com.slim.slimlauncher.settings.SettingsActivity;
import com.slim.slimlauncher.settings.SettingsProvider;

import java.net.URISyntaxException;
import java.util.List;

public class GestureHelper {

    public static final String TAG = "GestureHelper";

    public static final String ACTION_DEFAULT_HOMESCREEN = "default_homescreen";
    public static final String ACTION_OPEN_APP_DRAWER = "open_app_drawer";
    public static final String ACTION_OVERVIEW_MODE = "show_previews";
    public static final String ACTION_LAUNCHER_SETTINGS = "show_settings";
    public static final String ACTION_LAST_APP = "last_app";
    public static final String ACTION_CUSTOM = "custom";

    static WindowManager wm;
    static Display display;
    static Point size;
    static int width;
    static int height;
    static int sector;

    public enum Gesture {
        DOWN_LEFT,
        DOWN_MIDDLE,
        DOWN_RIGHT,
        UP_LEFT,
        UP_MIDDLE,
        UP_RIGHT,
        DOUBLE_TAP,
        NONE
    }

    static void init(Context context) {
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        sector = width / 3;
    }

    public static void performGestureAction(Launcher launcher,
                                            String gestureAction, String gesture) {
        if (wm == null) {
            init(launcher);
        }
        Workspace workspace = launcher.getWorkspace();
        if (gestureAction.equals(ACTION_DEFAULT_HOMESCREEN)) {
            workspace.setCurrentPage(workspace.getPageIndexForScreenId(workspace.getDefaultPage()));
        } else if (gestureAction.equals(ACTION_OPEN_APP_DRAWER)) {
            launcher.showAllApps(true, AppsCustomizePagedView.ContentType.Applications, true);
        } else if (gestureAction.equals(ACTION_OVERVIEW_MODE)) {
            workspace.enterOverviewMode(true);
        } else if (gestureAction.equals(ACTION_LAUNCHER_SETTINGS)) {
            Intent preferences = new Intent().setClass(launcher, SettingsActivity.class);
            preferences.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            launcher.startActivity(preferences);
        } else if (gestureAction.equals(ACTION_LAST_APP)) {
            getLastApp(launcher);
        } else if (gestureAction.equals(ACTION_CUSTOM)) {
            String uri = SettingsProvider.getString(launcher,
                    gesture + "_gesture_action_custom", "");
            if (!uri.equals("")) {
                try {
                    Intent intent = Intent.parseUri(uri, 0);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    launcher.startActivity(intent);
                } catch (URISyntaxException e) {
                    Log.e(TAG, "Unable to start gesture action " + gestureAction, e);
                }
            }
        }
    }

    private static void getLastApp(Launcher launcher) {
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

    public static Gesture identifyGesture(float upX, float upY, float downX, float downY) {

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

    public static boolean doesSwipeDownContainShowAllApps(Context context) {
        return (SettingsProvider.getString(context,
                SettingsProvider.LEFT_DOWN_GESTURE_ACTION, "").equals(ACTION_OPEN_APP_DRAWER)
                || SettingsProvider.getString(context,
                SettingsProvider.MIDDLE_DOWN_GESTURE_ACTION, "").equals(ACTION_OPEN_APP_DRAWER)
                || SettingsProvider.getString(context,
                SettingsProvider.RIGHT_DOWN_GESTURE_ACTION, "").equals(ACTION_OPEN_APP_DRAWER));
    }

    public static boolean doesSwipeUpContainShowAllApps(Context context) {
        return (SettingsProvider.getString(context,
                SettingsProvider.LEFT_UP_GESTURE_ACTION, "").equals(ACTION_OPEN_APP_DRAWER)
                || SettingsProvider.getString(context,
                SettingsProvider.MIDDLE_UP_GESTURE_ACTION, "").equals(ACTION_OPEN_APP_DRAWER)
                || SettingsProvider.getString(context,
                SettingsProvider.RIGHT_UP_GESTURE_ACTION, "").equals(ACTION_OPEN_APP_DRAWER));
    }

    public static boolean isSwipeDOWN(float upY, float downY) {
        return (upY - downY) > Workspace.MIN_UP_DOWN_GESTURE_DISTANCE;
    }

    public static boolean isSwipeUP(float upY, float downY) {
        return ((upY - downY) < -Workspace.MIN_UP_DOWN_GESTURE_DISTANCE);
    }

    public static boolean isSwipeLEFT(float downX) {
        return downX < sector;
    }

    public static boolean isSwipeMIDDLE(float downX) {
        return downX > sector && downX < (sector * 2);
    }

    public static boolean isSwipeRIGHT(float downX) {
        return downX > (sector * 2);
    }
}
