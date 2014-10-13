package com.slim.slimlauncher.settings;

import android.preference.PreferenceActivity;

import com.slim.slimlauncher.R;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences, target);
        updateHeaders(target);
    }

    private void updateHeaders(List<Header> headers) {
        for (Header header : headers) {
            if (header.id == R.id.slim_application_version) {
                header.title = getString(R.string.slim_application_name) + " "
                        + getString(R.string.slim_application_version);
            }
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }
}
