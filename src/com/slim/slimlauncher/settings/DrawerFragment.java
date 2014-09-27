package com.slim.slimlauncher.settings;

import android.os.Bundle;

import com.slim.slimlauncher.R;
import com.slim.slimlauncher.preference.DoubleNumberPickerPreference;

/**
 * Created by gmillz on 9/23/14.
 */
public class DrawerFragment extends SettingsPreferenceFragment {

    private DoubleNumberPickerPreference mDrawerGrid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, getString(R.string.drawer_title));

        addPreferencesFromResource(R.xml.drawer_preferences);

        mDrawerGrid = (DoubleNumberPickerPreference)
                findPreference(SettingsProvider.KEY_DRAWER_GRID);

        if (mProfile != null) {
            if (SettingsProvider.getCellCountX(getActivity(),
                    SettingsProvider.KEY_DRAWER_GRID, 0) < 1) {
                SettingsProvider.putCellCountX(getActivity(),
                        SettingsProvider.KEY_DRAWER_GRID, mProfile.allAppsNumCols);
                mDrawerGrid.setDefault2(mProfile.allAppsNumCols);
            }
            if (SettingsProvider.getCellCountY(getActivity(),
                    SettingsProvider.KEY_DRAWER_GRID, 0) < 1) {
                SettingsProvider.putCellCountY(getActivity(),
                        SettingsProvider.KEY_DRAWER_GRID, mProfile.allAppsNumRows);
                mDrawerGrid.setDefault2(mProfile.allAppsNumRows);
            }
        }
    }
}
