package com.android.launcher3.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.android.launcher3.R;

/**
 * Created by gmillz on 9/7/14.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
