/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.util.ComponentKey;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents an app in AllAppsView.
 */
public class AppInfo extends ItemInfo {
    static final int DOWNLOADED_FLAG = 1;
    static final int UPDATED_SYSTEM_APP_FLAG = 2;
    private static final String TAG = "Launcher3.AppInfo";
    /**
     * The intent used to start the application.
     */
    public Intent intent;
    /**
     * A bitmap version of the application icon.
     */
    public Bitmap iconBitmap;
    /**
     * The time at which the app was first installed.
     */
    public long firstInstallTime;
    public ComponentName componentName;
    /**
     * Indicates whether we're using a low res icon
     */
    boolean usingLowResIcon;

    int count;

    int unreadNum = 0;

    int flags = 0;

    private ArrayList<ShortcutInfo.ShortcutListener> mListeners = new ArrayList<>();

    AppInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }

    /**
     * Must not hold the Context.
     */
    public AppInfo(Context context, LauncherActivityInfoCompat info, UserHandleCompat user,
                   IconCache iconCache) {
        this.componentName = info.getComponentName();
        this.container = ItemInfo.NO_ID;

        flags = initFlags(info);
        firstInstallTime = info.getFirstInstallTime();
        iconCache.getTitleAndIcon(this, info, true /* useLowResIcon */);
        intent = makeLaunchIntent(context, info, user);
        this.user = user;
    }

    public AppInfo(AppInfo info) {
        super(info);
        componentName = info.componentName;
        title = Utilities.trim(info.title);
        intent = new Intent(info.intent);
        flags = info.flags;
        firstInstallTime = info.firstInstallTime;
        iconBitmap = info.iconBitmap;
    }

    public static int initFlags(LauncherActivityInfoCompat info) {
        int appFlags = info.getApplicationInfo().flags;
        int flags = 0;
        if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
            flags |= DOWNLOADED_FLAG;

            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                flags |= UPDATED_SYSTEM_APP_FLAG;
            }
        }
        return flags;
    }

    /**
     * Helper method used for debugging.
     */
    public static void dumpApplicationInfoList(String tag, String label, ArrayList<AppInfo> list) {
        Log.d(tag, label + " size=" + list.size());
        for (AppInfo info : list) {
            Log.d(tag, "   title=\"" + info.title + "\" iconBitmap=" + info.iconBitmap
                    + " firstInstallTime=" + info.firstInstallTime
                    + " componentName=" + info.componentName.getPackageName());
        }
    }

    public static Intent makeLaunchIntent(Context context, LauncherActivityInfoCompat info,
                                          UserHandleCompat user) {
        long serialNumber = UserManagerCompat.getInstance(context).getSerialNumberForUser(user);
        return new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(info.getComponentName())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .putExtra(EXTRA_PROFILE, serialNumber);
    }

    public Intent getIntent() {
        return intent;
    }

    protected Intent getRestoredIntent() {
        return null;
    }

    public void setIcon(Bitmap b) {
        iconBitmap = b;
        for (ShortcutInfo.ShortcutListener i : mListeners) {
            i.onIconChanged(this);
        }
    }

    public void setTitle(CharSequence title) {
        this.title = title;
        for (ShortcutInfo.ShortcutListener i : mListeners) {
            i.onIconChanged(this);
        }
    }

    public void addListener(ShortcutInfo.ShortcutListener listener) {
        if (!mListeners.contains(listener) && listener != null) {
            mListeners.add(listener);
        }
    }

    @Override
    public String toString() {
        return "ApplicationInfo(title=" + title + " id=" + this.id
                + " type=" + this.itemType + " container=" + this.container
                + " screen=" + screenId + " cellX=" + cellX + " cellY=" + cellY
                + " spanX=" + spanX + " spanY=" + spanY + " dropPos=" + Arrays.toString(dropPos)
                + " user=" + user + ")";
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }

    public ComponentKey toComponentKey() {
        return new ComponentKey(componentName, user);
    }
}
