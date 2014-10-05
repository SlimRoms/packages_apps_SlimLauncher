package com.slim.slimlauncher.settings;

import android.os.Bundle;

import com.slim.slimlauncher.R;

public class GeneralFragment extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_preferences);
    }
}
