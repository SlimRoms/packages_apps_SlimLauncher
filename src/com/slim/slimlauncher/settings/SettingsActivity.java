package com.slim.slimlauncher.settings;

import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.slim.slimlauncher.LauncherAppState;
import com.slim.slimlauncher.R;

import java.util.List;

public class SettingsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onResume() {
        super.onResume();
        SettingsProvider.get(this).registerOnSharedPreferenceChangeListener(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        SettingsProvider.get(this).unregisterOnSharedPreferenceChangeListener(this);
    }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        LauncherAppState.setSettingsChanged();
    }
}
