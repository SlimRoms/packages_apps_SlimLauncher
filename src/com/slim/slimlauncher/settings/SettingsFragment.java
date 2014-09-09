package com.slim.slimlauncher.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.slim.slimlauncher.DeviceProfile;
import com.slim.slimlauncher.DynamicGrid;
import com.slim.slimlauncher.LauncherAppState;
import com.slim.slimlauncher.R;
import com.slim.slimlauncher.preference.DoubleNumberPickerPreference;
import com.slim.slimlauncher.preference.NumberPickerPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private ColorPickerPreference mFolderBackground;

    private NumberPickerPreference mDockIcons;
    private DoubleNumberPickerPreference mHomescreenGrid;
    private DoubleNumberPickerPreference mDrawerGrid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        int intColor;
        String hexColor;

        mFolderBackground = (ColorPickerPreference)
                findPreference(SettingsProvider.FOLDER_BACKGROUND_COLOR);
        mFolderBackground.setAlphaSliderEnabled(true);
        mFolderBackground.setOnPreferenceChangeListener(this);
        mFolderBackground.setDefaultColor(0xffffffff);
        intColor = SettingsProvider.getInt(getActivity(),
                SettingsProvider.FOLDER_BACKGROUND_COLOR, -2);
        if (intColor == -2) {
            intColor = 0xffffffff;
            mFolderBackground.setSummary(getResources().getString(R.string.default_string));
        } else {
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mFolderBackground.setSummary(hexColor);
        }
        mFolderBackground.setNewPreviewColor(intColor);

        DynamicGrid grid = LauncherAppState.getInstance().getDynamicGrid();

        mDockIcons = (NumberPickerPreference)
                findPreference(SettingsProvider.KEY_DOCK_ICONS);
        mHomescreenGrid = (DoubleNumberPickerPreference)
                findPreference(SettingsProvider.KEY_HOMESCREEN_GRID);
        mDrawerGrid = (DoubleNumberPickerPreference)
                findPreference(SettingsProvider.KEY_DRAWER_GRID);

        Preference gestures = findPreference("gestures");
        gestures.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getFragmentManager().beginTransaction().replace(
                        android.R.id.content, new GestureFragment())
                        .addToBackStack("gesture").commit();
                return true;
            }
        });


        if (grid != null) {
            DeviceProfile prof = grid.getDeviceProfile();
            prof.updateFromPreferences(getActivity());

            if (SettingsProvider.getCellCountX(getActivity(),
                    SettingsProvider.KEY_HOMESCREEN_GRID, 0) < 1) {
                SettingsProvider.putCellCountX(getActivity(),
                        SettingsProvider.KEY_HOMESCREEN_GRID, (int) prof.numColumns);
                mHomescreenGrid.setDefault2((int) prof.numColumns);
            }
            if (SettingsProvider.getCellCountY(getActivity(),
                    SettingsProvider.KEY_HOMESCREEN_GRID, 0) < 1) {
                SettingsProvider.putCellCountY(getActivity(),
                        SettingsProvider.KEY_HOMESCREEN_GRID, (int) prof.numRows);
                mHomescreenGrid.setDefault1((int) prof.numRows);
            }
            if (SettingsProvider.getCellCountX(getActivity(),
                    SettingsProvider.KEY_DRAWER_GRID, 0) < 1) {
                SettingsProvider.putCellCountX(getActivity(),
                        SettingsProvider.KEY_DRAWER_GRID, prof.allAppsNumCols);
                mDrawerGrid.setDefault2(prof.allAppsNumCols);
            }
            if (SettingsProvider.getCellCountY(getActivity(),
                    SettingsProvider.KEY_DRAWER_GRID, 0) < 1) {
                SettingsProvider.putCellCountY(getActivity(),
                        SettingsProvider.KEY_DRAWER_GRID, prof.allAppsNumRows);
                mDrawerGrid.setDefault2(prof.allAppsNumRows);
            }
            if (SettingsProvider.getInt(getActivity(),
                    SettingsProvider.KEY_DOCK_ICONS, 0) < 1) {
                SettingsProvider.putInt(getActivity(),
                        SettingsProvider.KEY_DOCK_ICONS, (int) prof.numHotseatIcons);
                mDockIcons.setDefaultValue((int) prof.numHotseatIcons);
            }

        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
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

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFolderBackground) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            SettingsProvider.putInt(getActivity(),
                    SettingsProvider.FOLDER_BACKGROUND_COLOR, intHex);
            return true;
        }
        return false;
    }
}
