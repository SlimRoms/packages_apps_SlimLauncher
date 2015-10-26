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
    private DoubleNumberPickerPreference mLandscapeDrawerGrid;
    private ListPreference mDrawerType;
    Preference.OnPreferenceChangeListener mPreferenceChangeListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    updatePrefs();
                    return true;
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.drawer_preferences);

        mDrawerType = (ListPreference)
                findPreference(SettingsProvider.KEY_DRAWER_TYPE);
        mDrawerType.setOnPreferenceChangeListener(mPreferenceChangeListener);

        mPortraitDrawerGrid = (DoubleNumberPickerPreference)
                findPreference(SettingsProvider.KEY_PORTRAIT_DRAWER_GRID);
        mLandscapeDrawerGrid = (DoubleNumberPickerPreference)
                findPreference(SettingsProvider.KEY_LANDSCAPE_DRAWER_GRID);

        if (mProfile != null) {

            String[] settingsKeys = {SettingsProvider.KEY_PORTRAIT_DRAWER_GRID,
                    SettingsProvider.KEY_LANDSCAPE_DRAWER_GRID};

            for (String key : settingsKeys) {
                if (SettingsProvider.getCellCountX(getActivity(), key, 0) < 1) {
                    if (SettingsProvider.KEY_PORTRAIT_DRAWER_GRID.equals(key)) {
                        SettingsProvider.putCellCountX(getActivity(), key,
                                mProfile.portraitProfile.pagedAllAppsNumCols);
                        mPortraitDrawerGrid.setDefault2(
                                mProfile.portraitProfile.pagedAllAppsNumCols);
                    } else if (SettingsProvider.KEY_LANDSCAPE_DRAWER_GRID.equals(key)) {
                        SettingsProvider.putCellCountX(getActivity(), key,
                                mProfile.landscapeProfile.pagedAllAppsNumCols);
                        mLandscapeDrawerGrid.setDefault2(
                                mProfile.landscapeProfile.pagedAllAppsNumCols);
                    }
                }
                if (SettingsProvider.getCellCountY(getActivity(), key, 0) < 1) {
                    if (SettingsProvider.KEY_PORTRAIT_DRAWER_GRID.equals(key)) {
                        SettingsProvider.putCellCountY(getActivity(), key,
                                mProfile.portraitProfile.pagedAllAppsNumRows);
                        mPortraitDrawerGrid.setDefault1(
                                mProfile.portraitProfile.pagedAllAppsNumRows);
                    } else if (SettingsProvider.KEY_LANDSCAPE_DRAWER_GRID.equals(key)) {
                        SettingsProvider.putCellCountY(getActivity(), key,
                                mProfile.landscapeProfile.pagedAllAppsNumRows);
                        mLandscapeDrawerGrid.setDefault1(
                                mProfile.landscapeProfile.pagedAllAppsNumRows);
                    }
                }
            }
        }
        updatePrefs();
    }

    public void updatePrefs() {
        if (Integer.parseInt(mDrawerType.getValue()) == Launcher.DRAWER_TYPE_VERTICAL) {
            mPortraitDrawerGrid.setEnabled(false);
            mLandscapeDrawerGrid.setEnabled(false);
        } else {
            mPortraitDrawerGrid.setEnabled(true);
            mLandscapeDrawerGrid.setEnabled(true);
        }
    }
}
