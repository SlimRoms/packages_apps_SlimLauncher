package com.slim.slimlauncher.settings;

import android.os.Bundle;
import android.preference.Preference;

import com.slim.slimlauncher.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class FolderFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private ColorPickerPreference mFolderBackground;
    private ColorPickerPreference mFolderIconTextColor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.folder_preferences);

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
            hexColor = String.format("#%08x", (intColor));
            mFolderBackground.setSummary(hexColor);
        }
        mFolderBackground.setNewPreviewColor(intColor);

        mFolderIconTextColor = (ColorPickerPreference)
                findPreference(SettingsProvider.FOLDER_ICON_TEXT_COLOR);
        mFolderIconTextColor.setAlphaSliderEnabled(true);
        mFolderIconTextColor.setOnPreferenceChangeListener(this);
        mFolderIconTextColor.setDefaultColor(
                getResources().getColor(R.color.folder_items_text_color));
        intColor = SettingsProvider.getInt(getActivity(),
                SettingsProvider.FOLDER_ICON_TEXT_COLOR, -2);
        if (intColor == -2) {
            intColor = getResources().getColor(R.color.folder_items_text_color);
            mFolderIconTextColor.setSummary(getString(R.string.default_string));
        } else {
            hexColor = String.format("#%08x", (intColor));
            mFolderIconTextColor.setSummary(hexColor);
        }
        mFolderIconTextColor.setNewPreviewColor(intColor);
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
        }
        return true;
    }
}
