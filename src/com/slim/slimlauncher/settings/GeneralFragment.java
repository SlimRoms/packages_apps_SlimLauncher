/*
 * Copyright (C) 2015 The SlimRoms Project
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

package com.slim.slimlauncher.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;

import com.slim.slimlauncher.IconPackHelper;
import com.slim.slimlauncher.NotificationListener;
import com.slim.slimlauncher.R;

public class GeneralFragment extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    SwitchPreference mNotificationBadges;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_preferences);

        mNotificationBadges = (SwitchPreference)
                findPreference(SettingsProvider.KEY_NOTIFICATION_BADGES);
        if (!NotificationListener.isEnabled(mContext)) {
            mNotificationBadges.setChecked(false);
        }
        mNotificationBadges.setOnPreferenceChangeListener(this);

        Preference iconPack = findPreference(SettingsProvider.KEY_ICON_PACK);
        iconPack.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                IconPackHelper.pickIconPack(mContext, false);
                return true;
            }
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNotificationBadges) {
            if (!NotificationListener.isEnabled(mContext)) {
                startNotificationListenerSettings();
            }
            return true;
        }
        return false;
    }

    private void startNotificationListenerSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.notification_badge_warning_message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(
                            new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    }
        });
        builder.show();
    }
}
