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

package com.android.launcher3.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.preference.DoubleNumberPickerPreference;

public class DrawerFragment extends SettingsPreferenceFragment {

    private DoubleNumberPickerPreference mPortraitDrawerGrid;
    private ListPreference mDrawerType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.drawer_preferences);

        mDrawerType = (ListPreference)
                findPreference(SettingsProvider.KEY_DRAWER_TYPE);
        mDrawerType.setOnPreferenceChangeListener(mPreferenceChangeListener);

        mPortraitDrawerGrid = (DoubleNumberPickerPreference)
                findPreference(SettingsProvider.KEY_DRAWER_GRID);

        if (mProfile != null) {
            if (SettingsProvider.getCellCountX(getActivity(),
                    SettingsProvider.KEY_DRAWER_GRID, 0) < 1) {
                SettingsProvider.putCellCountX(getActivity(),
                        SettingsProvider.KEY_DRAWER_GRID,
                        mProfile.portraitProfile.pagedAllAppsNumCols);
                mPortraitDrawerGrid.setDefault2(mProfile.portraitProfile.pagedAllAppsNumCols);
            }
            if (SettingsProvider.getCellCountY(getActivity(),
                    SettingsProvider.KEY_DRAWER_GRID, 0) < 1) {
                SettingsProvider.putCellCountY(getActivity(),
                        SettingsProvider.KEY_DRAWER_GRID,
                        mProfile.portraitProfile.pagedAllAppsNumRows);
                mPortraitDrawerGrid.setDefault1(mProfile.portraitProfile.pagedAllAppsNumRows);
            }
        }
        updatePrefs();
    }

    public void updatePrefs() {
        if (Integer.parseInt(mDrawerType.getValue()) == Launcher.DRAWER_TYPE_VERTICAL) {
            mPortraitDrawerGrid.setEnabled(false);
        } else {
            mPortraitDrawerGrid.setEnabled(true);
        }
    }

    Preference.OnPreferenceChangeListener mPreferenceChangeListener =
            new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            updatePrefs();
            return true;
        }
    };
}
