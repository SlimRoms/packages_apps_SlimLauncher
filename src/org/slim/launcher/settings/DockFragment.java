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

package org.slim.launcher.settings;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.android.launcher3.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import org.slim.launcher.preference.NumberPickerPreference;

public class DockFragment extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.dock_preferences);

        final NumberPickerPreference dockIcons = (NumberPickerPreference)
                findPreference(SettingsProvider.KEY_DOCK_ICONS);

        ColorPickerPreference dockColor = (ColorPickerPreference)
                findPreference(SettingsProvider.KEY_DOCK_BACKGROUND);

        dockColor.setDefaultColor(ContextCompat.getColor(mContext, R.color.hotseat_default_color));

        //noinspection ResourceType
        if (!mContext.getString(R.color.hotseat_background_color).equalsIgnoreCase("#0")) {
            dockColor.setEnabled(false);
        }

        if (mProfile != null) {
            if (SettingsProvider.getInt(getActivity(),
                    SettingsProvider.KEY_DOCK_ICONS, 0) < 1) {
                SettingsProvider.putInt(getActivity(),
                        SettingsProvider.KEY_DOCK_ICONS, mProfile.numHotseatIcons);
            }
            dockIcons.setDefaultValue(mProfile.numHotseatIcons);
        }
    }
}
