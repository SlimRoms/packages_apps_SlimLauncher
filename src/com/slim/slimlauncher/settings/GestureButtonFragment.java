package com.slim.slimlauncher.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import com.slim.slimlauncher.R;
import com.slim.slimlauncher.util.AppHelper;
import com.slim.slimlauncher.util.ShortcutPickHelper;

public class GestureButtonFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    ShortcutPickHelper mPicker;

    ListPreference mGestureLeftUp;
    ListPreference mGestureMiddleUp;
    ListPreference mGestureRightUp;
    ListPreference mGestureLeftDown;
    ListPreference mGestureMiddleDown;
    ListPreference mGestureRightDown;
    ListPreference mGesturePinch;
    ListPreference mGestureSpread;
    ListPreference mGestureDoubleTap;

    ListPreference mHomeButton;
    ListPreference mMenuButton;
    ListPreference mBackButton;

    String mGesture;
    ListPreference mGestureCurrent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.gesture_button_fragment);

        mGestureLeftUp = (ListPreference) findPreference(SettingsProvider.LEFT_UP_GESTURE_ACTION);
        mGestureMiddleUp = (ListPreference)
                findPreference(SettingsProvider.MIDDLE_UP_GESTURE_ACTION);
        mGestureRightUp = (ListPreference) findPreference(SettingsProvider.RIGHT_UP_GESTURE_ACTION);
        mGestureLeftDown = (ListPreference)
                findPreference(SettingsProvider.LEFT_DOWN_GESTURE_ACTION);
        mGestureMiddleDown = (ListPreference)
                findPreference(SettingsProvider.MIDDLE_DOWN_GESTURE_ACTION);
        mGestureRightDown = (ListPreference)
                findPreference(SettingsProvider.RIGHT_DOWN_GESTURE_ACTION);
        mGesturePinch = (ListPreference) findPreference(SettingsProvider.PINCH_GESTURE_ACTION);
        mGestureSpread = (ListPreference) findPreference(SettingsProvider.SPREAD_GESTURE_ACTION);
        mGestureDoubleTap = (ListPreference)
                findPreference(SettingsProvider.DOUBLE_TAP_GESTURE_ACTION);

        mGestureLeftUp.setOnPreferenceChangeListener(this);
        mGestureMiddleUp.setOnPreferenceChangeListener(this);
        mGestureRightUp.setOnPreferenceChangeListener(this);
        mGestureLeftDown.setOnPreferenceChangeListener(this);
        mGestureMiddleDown.setOnPreferenceChangeListener(this);
        mGestureRightDown.setOnPreferenceChangeListener(this);
        mGesturePinch.setOnPreferenceChangeListener(this);
        mGestureSpread.setOnPreferenceChangeListener(this);
        mGestureDoubleTap.setOnPreferenceChangeListener(this);

        mHomeButton = (ListPreference) findPreference(SettingsProvider.KEY_HOME_BUTTON_ACTION);
        mMenuButton = (ListPreference) findPreference(SettingsProvider.KEY_MENU_BUTTON_ACTION);
        mBackButton = (ListPreference) findPreference(SettingsProvider.KEY_BACK_BUTTON_ACTION);

        mHomeButton.setOnPreferenceChangeListener(this);
        mMenuButton.setOnPreferenceChangeListener(this);
        mBackButton.setOnPreferenceChangeListener(this);

        mPicker = new ShortcutPickHelper(getActivity(), new ShortcutPickHelper.OnPickListener() {
            @Override
            public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
                if (uri == null) {
                    return;
                }
                if (mGesture != null) {
                    SettingsProvider.putString(mContext, mGesture + "_custom", uri);
                    mGesture = null;
                }
                if (mGestureCurrent != null) {
                    updateSummary(mGestureCurrent, "custom");
                    mGestureCurrent = null;
                }
            }
        });

        updatePrefs();
    }

    private void updatePrefs() {
        updateSummary(mGestureLeftUp, mGestureLeftUp.getValue());
        updateSummary(mGestureMiddleUp, mGestureMiddleUp.getValue());
        updateSummary(mGestureRightUp, mGestureRightUp.getValue());
        updateSummary(mGestureLeftDown, mGestureLeftDown.getValue());
        updateSummary(mGestureMiddleDown, mGestureMiddleDown.getValue());
        updateSummary(mGestureRightDown, mGestureRightDown.getValue());
        updateSummary(mGesturePinch, mGesturePinch.getValue());
        updateSummary(mGestureSpread, mGestureSpread.getValue());
        updateSummary(mGestureDoubleTap, mGestureDoubleTap.getValue());
        updateSummary(mHomeButton, mHomeButton.getValue());
        updateSummary(mMenuButton, mMenuButton.getValue());
        updateSummary(mBackButton, mBackButton.getValue());
    }

    private void updateSummary(ListPreference pref, String value) {
        if (value.equals("custom")) {
            pref.setSummary(AppHelper.getFriendlyNameForUri(
                    getActivity(), getActivity().getPackageManager(),
                    SettingsProvider.getString(getActivity(), pref.getKey() + "_custom", "")));
        } else {
            CharSequence[] array = pref.getEntryValues();
            for (int i = 0; i < array.length; i++) {
                if (array[i].equals(value)) {
                    pref.setSummary(pref.getEntries()[i]);
                    return;
                }
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mGestureLeftUp || preference == mGestureMiddleUp ||
                preference == mGestureRightUp || preference == mGestureLeftDown||
                preference == mGestureMiddleDown || preference == mGestureRightDown ||
                preference == mGesturePinch || preference == mGestureSpread ||
                preference == mGestureDoubleTap || preference == mHomeButton ||
                preference == mMenuButton || preference == mBackButton) {
            if (newValue.equals("custom")) {
                mGesture = preference.getKey();
                mGestureCurrent = (ListPreference) preference;
                mPicker.pickShortcut(null, null, getId());
                return true;
            }
            updateSummary((ListPreference) preference, (String) newValue);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != Activity.RESULT_CANCELED
                && resultCode != Activity.RESULT_CANCELED) {
            mPicker.onActivityResult(requestCode, resultCode, data);
        }
    }
}
