package com.slim.slimlauncher.settings;

import android.preference.PreferenceActivity;

import com.slim.slimlauncher.R;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences, target);
    }
}
