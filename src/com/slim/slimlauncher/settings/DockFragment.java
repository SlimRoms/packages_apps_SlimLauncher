package com.slim.slimlauncher.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import com.slim.slimlauncher.R;
import com.slim.slimlauncher.preference.NumberPickerPreference;

public class DockFragment extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.dock_preferences);

        boolean hide = SettingsProvider.getBoolean(mContext,
                SettingsProvider.KEY_HIDE_DOCK, false);

        final NumberPickerPreference dockIcons = (NumberPickerPreference)
                findPreference(SettingsProvider.KEY_DOCK_ICONS);

        final CheckBoxPreference hideDock = (CheckBoxPreference)
                findPreference(SettingsProvider.KEY_HIDE_DOCK);

        final CheckBoxPreference hideLabels = (CheckBoxPreference)
                findPreference(SettingsProvider.KEY_DOCK_HIDE_LABELS);

        Preference.OnPreferenceChangeListener onPreferenceChangeListener =
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        dockIcons.setEnabled(hideDock.isChecked());
                        hideLabels.setEnabled(hideDock.isChecked());
                        return true;
                    }
                };

        hideDock.setOnPreferenceChangeListener(onPreferenceChangeListener);

        dockIcons.setEnabled(!hide);
        hideLabels.setEnabled(!hide);

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
