package com.slim.slimlauncher.settings;

import android.os.Bundle;

import com.slim.slimlauncher.R;
import com.slim.slimlauncher.preference.NumberPickerPreference;

public class DockFragment extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, getString(R.string.dock_title));

        addPreferencesFromResource(R.xml.dock_preferences);

        NumberPickerPreference dockIcons = (NumberPickerPreference)
                findPreference(SettingsProvider.KEY_DOCK_ICONS);

        if (mProfile != null) {
            if (SettingsProvider.getInt(getActivity(),
                    SettingsProvider.KEY_DOCK_ICONS, 0) < 1) {
                SettingsProvider.putInt(getActivity(),
                        SettingsProvider.KEY_DOCK_ICONS, (int) mProfile.numHotseatIcons);
                dockIcons.setDefaultValue((int) mProfile.numHotseatIcons);
            }
        }

    }
}
