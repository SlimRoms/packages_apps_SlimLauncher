package com.slim.slimlauncher.settings;

import android.os.Bundle;

import com.slim.slimlauncher.R;
import com.slim.slimlauncher.preference.DoubleNumberPickerPreference;

public class HomescreenFragment extends SettingsPreferenceFragment {

    private DoubleNumberPickerPreference mHomescreenGrid;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.homescreen_preferences);

        mHomescreenGrid = (DoubleNumberPickerPreference)
                findPreference(SettingsProvider.KEY_HOMESCREEN_GRID);


        if (mProfile != null) {
            if (SettingsProvider.getCellCountX(getActivity(),
                    SettingsProvider.KEY_HOMESCREEN_GRID, 0) < 1) {
                SettingsProvider.putCellCountX(getActivity(),
                        SettingsProvider.KEY_HOMESCREEN_GRID, (int) mProfile.numColumns);
                mHomescreenGrid.setDefault2((int) mProfile.numColumns);
            }
            if (SettingsProvider.getCellCountY(getActivity(),
                    SettingsProvider.KEY_HOMESCREEN_GRID, 0) < 1) {
                SettingsProvider.putCellCountY(getActivity(),
                        SettingsProvider.KEY_HOMESCREEN_GRID, (int) mProfile.numRows);
                mHomescreenGrid.setDefault1((int) mProfile.numRows);
            }
        }
    }
}
