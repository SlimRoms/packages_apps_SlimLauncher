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

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class DrawerFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
        
    private ColorPickerPreference mBackground;

    private DoubleNumberPickerPreference mPortraitDrawerGrid;
    private DoubleNumberPickerPreference mLandscapeDrawerGrid;
    private ListPreference mDrawerType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.drawer_preferences);

        mDrawerType = (ListPreference)
                findPreference(SettingsProvider.KEY_DRAWER_TYPE);
        mDrawerType.setOnPreferenceChangeListener(this);

        mPortraitDrawerGrid = (DoubleNumberPickerPreference)
                findPreference(SettingsProvider.KEY_PORTRAIT_DRAWER_GRID);
        mLandscapeDrawerGrid = (DoubleNumberPickerPreference)
                findPreference(SettingsProvider.KEY_LANDSCAPE_DRAWER_GRID);

 mBackground = (ColorPickerPreference)
                findPreference(SettingsProvider.APP_BGD_COLOR);
        mBackground.setAlphaSliderEnabled(true);
        mBackground.setOnPreferenceChangeListener(this);
        intColor = SettingsProvider.getInt(getActivity(),
                SettingsProvider.APP_BGD_COLOR, -2);
        if (intColor == -2) {
            intColor = 0xffffffff;
            mBackground.setSummary(getResources().getString(R.string.default_string));
        } else {
            hexColor = String.format("#%08x", (intColor));
            mBackground.setSummary(hexColor);
        }


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

        updateGridPrefs(Integer.parseInt(SettingsProvider.getString(mContext,
                SettingsProvider.KEY_DRAWER_TYPE, "0")));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (!preference.getKey().equals(SettingsProvider.KEY_DRAWER_TYPE)) return false;
         if (preference == mBackground) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            SettingsProvider.putInt(getActivity(),
                    SettingsProvider.APP_BGD_COLOR, intHex);
        }
        updateGridPrefs(Integer.parseInt((String)o));
        return true;
    }

    private void updateGridPrefs(int drawerType) {
        if (drawerType == Launcher.DRAWER_TYPE_PAGED) {
            mPortraitDrawerGrid.setEnabled(true);
            mLandscapeDrawerGrid.setEnabled(true);
        } else {
            mPortraitDrawerGrid.setEnabled(false);
            mLandscapeDrawerGrid.setEnabled(false);
        }
    }
}
