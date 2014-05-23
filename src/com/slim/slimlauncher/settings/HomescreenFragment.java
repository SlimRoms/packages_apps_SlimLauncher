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

import android.os.Bundle;
import android.preference.Preference;

import com.slim.slimlauncher.R;
import com.slim.slimlauncher.preference.DoubleNumberPickerPreference;

public class HomescreenFragment extends SettingsPreferenceFragment {

    private DoubleNumberPickerPreference mHomescreenGrid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.homescreen_preferences);

        mHomescreenGrid = (DoubleNumberPickerPreference)
                findPreference(SettingsProvider.KEY_HOMESCREEN_GRID);

        Preference customHotwords = findPreference("custom_hotwords");
        /*customHotwords.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getFragmentManager().beginTransaction().replace(
                        android.R.id.content, new HotwordCustomFragment())
                        .addToBackStack(preference.getKey()).commit();
                return true;
            }
        });*/


        if (mProfile != null) {
            if (SettingsProvider.getCellCountX(getActivity(),
                    SettingsProvider.KEY_HOMESCREEN_GRID, 0) < 1) {
                SettingsProvider.putCellCountX(getActivity(),
                        SettingsProvider.KEY_HOMESCREEN_GRID, (int) mProfile.numColumns);
                mHomescreenGrid.setDefault2((int) mProfile.numColumns);
            }
            if (SettingsProvider.getCellCountY(getActivity(),
                    SettingsProvider.KEY_HOMESCREEN_GRID, 0) < 1) {
                SettingsProvider.putCellCountY(getActivity(),
                        SettingsProvider.KEY_HOMESCREEN_GRID, (int) mProfile.numRows);
                mHomescreenGrid.setDefault1((int) mProfile.numRows);
            }
        }
    }
}
