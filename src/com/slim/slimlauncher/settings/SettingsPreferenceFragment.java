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

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;

import com.slim.slimlauncher.DeviceProfile;
import com.slim.slimlauncher.DynamicGrid;
import com.slim.slimlauncher.LauncherAppState;

public class SettingsPreferenceFragment extends PreferenceFragment {

    DynamicGrid mGrid;
    DeviceProfile mProfile;

    LayoutInflater mInflater;

    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGrid = LauncherAppState.getInstance().getDynamicGrid();
        if (mGrid != null) {
            mProfile = mGrid.getDeviceProfile();
            mProfile.updateFromPreferences(getActivity());
        }

        mContext = getActivity();

        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
