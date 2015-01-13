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
import android.preference.Preference;

import com.android.launcher3.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class FolderFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private ColorPickerPreference mFolderBackground;
    private ColorPickerPreference mFolderIconTextColor;
    private ColorPickerPreference mFolderPreviewColor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.folder_preferences);

        int intColor;
        String hexColor;

        mFolderBackground = (ColorPickerPreference)
                findPreference(SettingsProvider.FOLDER_BACKGROUND_COLOR);
        mFolderBackground.setAlphaSliderEnabled(true);
        mFolderBackground.setOnPreferenceChangeListener(this);
        mFolderBackground.setDefaultColor(0xffffffff);
        intColor = SettingsProvider.getInt(getActivity(),
                SettingsProvider.FOLDER_BACKGROUND_COLOR, -2);
        if (intColor == -2) {
            intColor = 0xffffffff;
            mFolderBackground.setSummary(getResources().getString(R.string.default_string));
        } else {
            hexColor = String.format("#%08x", (intColor));
            mFolderBackground.setSummary(hexColor);
        }
        mFolderBackground.setNewPreviewColor(intColor);

        mFolderIconTextColor = (ColorPickerPreference)
                findPreference(SettingsProvider.FOLDER_ICON_TEXT_COLOR);
        mFolderIconTextColor.setAlphaSliderEnabled(true);
        mFolderIconTextColor.setOnPreferenceChangeListener(this);
        mFolderIconTextColor.setDefaultColor(
                getResources().getColor(R.color.folder_items_text_color));
        intColor = SettingsProvider.getInt(getActivity(),
                SettingsProvider.FOLDER_ICON_TEXT_COLOR, -2);
        if (intColor == -2) {
            intColor = getResources().getColor(R.color.folder_items_text_color);
            mFolderIconTextColor.setSummary(getString(R.string.default_string));
        } else {
            hexColor = String.format("#%08x", (intColor));
            mFolderIconTextColor.setSummary(hexColor);
        }
        mFolderIconTextColor.setNewPreviewColor(intColor);

        mFolderPreviewColor = (ColorPickerPreference)
                findPreference(SettingsProvider.FOLDER_PREVIEW_COLOR);
        mFolderPreviewColor.setAlphaSliderEnabled(true);
        mFolderPreviewColor.setOnPreferenceChangeListener(this);
        mFolderPreviewColor.setDefaultColor(0x71ffffff);
        intColor = SettingsProvider.getInt(mContext,
                SettingsProvider.FOLDER_PREVIEW_COLOR, -2);
        if (intColor == -2) {
            intColor = 0xffffffff;
            mFolderPreviewColor.setSummary(getString(R.string.default_string));
        } else {
            hexColor = String.format("#%08x", (intColor));
            mFolderPreviewColor.setSummary(hexColor);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFolderBackground) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            SettingsProvider.putInt(getActivity(),
                    SettingsProvider.FOLDER_BACKGROUND_COLOR, intHex);
        } else if (preference == mFolderIconTextColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf((String.valueOf(newValue))));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            SettingsProvider.putInt(getActivity(),
                    SettingsProvider.FOLDER_ICON_TEXT_COLOR, intHex);
        }
        return true;
    }
}
