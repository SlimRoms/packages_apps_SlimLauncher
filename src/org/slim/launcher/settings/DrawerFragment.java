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

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;

import com.android.launcher3.R;

import org.slim.launcher.util.AllAppsActivity;

public class DrawerFragment extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.drawer_preferences);
        addHideAppsListener();
    }

    private void addHideAppsListener() {

        Preference reset = findPreference("hide_apps");
        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference pref) {

                startActivity(new Intent(getActivity(), AllAppsActivity.class));
                getActivity().finish();

                return true;
            }
        });
    }
}
