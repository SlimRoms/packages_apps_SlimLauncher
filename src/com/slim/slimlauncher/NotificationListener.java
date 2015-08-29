/*
 * Copyright (C) 2013 The ChameleonOS Open Source Project
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

package com.slim.slimlauncher;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.HashMap;
import java.util.Map;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";
    private ListenerStatusObserver mStatusObserver;
    private static Map<String, String> mMappings = new HashMap<String, String>();

    static {
        mMappings.put("com.android.phone", "com.android.dialer");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStatusObserver = new ListenerStatusObserver();
        mStatusObserver.observe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStatusObserver.unobserve();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isEnabled(getApplicationContext()))
            updateCurrentNotifications();
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateCurrentNotifications() {
        StatusBarNotification[] notifications = null;
        try {
            notifications = getActiveNotifications();
        } catch (Exception e) {}
        if (notifications != null && notifications.length > 0) {
            for (StatusBarNotification sbn : notifications) {
                onNotificationPosted(sbn);
            }
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!sbn.isOngoing()) {
            int count = sbn.getNotification().number;
            broadcastNotificationUpdate(sbn.getPackageName(), sbn.getId(),
                    count == 0 ? 1 : count);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (!sbn.isOngoing()) {
            broadcastNotificationUpdate(sbn.getPackageName(), sbn.getId(),
                    0);
        }
    }

    private void broadcastNotificationUpdate(String packageName, int id, int count) {
        if (mMappings.containsKey(packageName))
            packageName = mMappings.get(packageName);
        Intent intent = new Intent(LauncherModel.ACTION_UNREAD_CHANGED);
        intent.putExtra("packageName", packageName);
        intent.putExtra("count", count);
        intent.putExtra("id", id);
        sendBroadcast(intent);
    }

    /**
     * This is just a simple ContentObserver class used to listen for
     * changes to the list of enabled notification listeners.
     * You'll need to create an instance of this class and call observe()
     * to listen for changes and unobserve() when you no longer need it.
     */
    class ListenerStatusObserver extends ContentObserver {
        /**
         * Note: Settings.Secure.ENABLED_NOTIFICATION_LISTENERS is hidden from
         * the public API so you'll want to replace it with the actual string
         * which is "enabled_notification_listeners"
         */

        public ListenerStatusObserver() {
            super(new Handler());
        }

        public void observe() {
            ContentResolver cr = getContentResolver();
            cr.registerContentObserver(Settings.Secure.getUriFor(
                    "enabled_notification_listeners"), false, this);
        }

        public void unobserve() {
            getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            update();
        }

        public void update() {
            boolean enabled = isEnabled(getApplicationContext());
            if (enabled) {
                updateCurrentNotifications();
            } else {
                Intent intent = new Intent(LauncherModel.ACTION_UNREAD_CHANGED);
                intent.putExtra("packageName", "");
                intent.putExtra("count", 0);
                intent.putExtra("id", -1);
                sendBroadcast(intent);
            }
        }
    }

    public static boolean isEnabled(Context context) {
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                "enabled_notification_listeners");
        boolean enabled = false;
        if (flat != null && !"".equals(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                if (names[i].contains(context.getPackageName())) {
                    enabled = true;
                }
            }
        }
        return enabled;
    }
}
