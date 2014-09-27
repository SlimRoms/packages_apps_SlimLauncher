package com.slim.slimlauncher.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.slim.slimlauncher.R;

public class SettingsFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, false);

        addPreferencesFromResource(R.xml.preferences);

        Preference gestures = findPreference("gestures");
        if (gestures != null)
            gestures.setOnPreferenceClickListener(this);

        Preference homescreen = findPreference("key_homescreen");
        homescreen.setOnPreferenceClickListener(this);
        Preference folder = findPreference("key_folder");
        folder.setOnPreferenceClickListener(this);

        Preference drawer = findPreference("key_drawer");
        drawer.setOnPreferenceClickListener(this);

        Preference dock = findPreference("key_dock");
        dock.setOnPreferenceClickListener(this);

        setHasOptionsMenu(true);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        SettingsPreferenceFragment fragment = null;
        if (preference.getKey().equals("key_homescreen")) {
            fragment = new HomescreenFragment();
        } else if (preference.getKey().equals("key_folder")) {
            fragment = new FolderFragment();
        } else if (preference.getKey().equals("gestures")) {
            fragment = new GestureFragment();
        } else if (preference.getKey().equals("key_drawer")) {
            fragment = new DrawerFragment();
        } else if (preference.getKey().equals("key_dock")) {
            fragment = new DockFragment();
        }
        if (fragment != null) {
            getFragmentManager().beginTransaction().replace(android.R.id.content, fragment)
                    .addToBackStack(preference.getKey()).commit();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onContextItemSelected(item);
        }
    }
}
