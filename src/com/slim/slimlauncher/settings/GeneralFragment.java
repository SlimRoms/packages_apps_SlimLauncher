package com.slim.slimlauncher.settings;

import android.os.Bundle;
import android.preference.Preference;

import com.slim.slimlauncher.IconPackHelper;
import com.slim.slimlauncher.R;

public class GeneralFragment extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_preferences);

        Preference iconPack = findPreference(SettingsProvider.KEY_ICON_PACK);
        iconPack.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                IconPackHelper.pickIconPack(mContext, false);
                return true;
            }
        });
    }
}
