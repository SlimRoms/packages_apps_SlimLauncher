package com.slim.slimlauncher.settings;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.slim.slimlauncher.NotificationListener;
import com.slim.slimlauncher.R;
import com.slim.slimlauncher.preference.AppMultiSelectListPreference;
import com.slim.slimlauncher.preference.SlimSeekBarPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class NotificationBadge extends SettingsPreferenceFragment
        implements OnCheckedChangeListener, Preference.OnPreferenceChangeListener {

    private ColorPickerPreference mNotificationBadgeColor;
    private ColorPickerPreference mNotificationBadgeTextColor;
    private SlimSeekBarPreference mNotificationBadgeTextSize;
    private SlimSeekBarPreference mNotificationBadgeCornerRadius;
    private AppMultiSelectListPreference mNotificationBadgeExcludedApps;

    private static final float TEXT_SIZE_DEFAULT = 33.0f;
    private static final float TEXT_SIZE_MAX = 50.0f;
    private static final float TEXT_SIZE_MIN = 10.0f;

    private static final float CORNER_RADIUS_DEFAULT = 21.0f;
    private static final float CORNER_RADIUS_MAX = 50.0f;
    private static final float CORNER_RADIUS_MIN = 3.0f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notification_badge_preferences);

        ActionBar actionbar = getActivity().getActionBar();
		Switch actionBarSwitch = new Switch(mContext);

		actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM);
		actionbar.setCustomView(actionBarSwitch, new ActionBar.LayoutParams(
				ActionBar.LayoutParams.WRAP_CONTENT,
				ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
						| Gravity.RIGHT));

		actionbar.setTitle(mContext.getString(R.string.notification_badges_title));

        actionBarSwitch.setOnCheckedChangeListener(this);
        actionBarSwitch.setChecked(SettingsProvider.getBoolean(mContext,
                SettingsProvider.KEY_NOTIFICATION_BADGES, false));

        int intColor;
        String hexColor;

        mNotificationBadgeColor = (ColorPickerPreference)
                findPreference(SettingsProvider.KEY_NOTIFICATION_BADGE_COLOR);
        mNotificationBadgeColor.setAlphaSliderEnabled(true);
        mNotificationBadgeColor.setOnPreferenceChangeListener(this);
        mNotificationBadgeColor.setDefaultColor(
                getResources().getColor(R.color.infomation_count_circle_color));
        intColor = SettingsProvider.getInt(mContext,
                SettingsProvider.KEY_NOTIFICATION_BADGE_COLOR, -2);
        if (intColor == -2) {
            intColor = getResources().getColor(R.color.infomation_count_circle_color);
            mNotificationBadgeColor.setSummary(getString(R.string.default_string));
        } else {
            hexColor = String.format("#%08x", (intColor));
            mNotificationBadgeColor.setSummary(hexColor);
        }
        mNotificationBadgeColor.setNewPreviewColor(intColor);

        mNotificationBadgeTextColor = (ColorPickerPreference)
                findPreference(SettingsProvider.KEY_NOTIFICATION_BADGE_TEXT_COLOR);
        mNotificationBadgeTextColor.setAlphaSliderEnabled(true);
        mNotificationBadgeTextColor.setOnPreferenceChangeListener(this);
        mNotificationBadgeTextColor.setDefaultColor(Color.WHITE);
        intColor = SettingsProvider.getInt(mContext,
                SettingsProvider.KEY_NOTIFICATION_BADGE_TEXT_COLOR, -2);
        if (intColor == -2) {
            intColor = Color.WHITE;
            mNotificationBadgeTextColor.setSummary(getString(R.string.default_string));
        } else {
            hexColor = String.format("#%08x", (intColor));
            mNotificationBadgeTextColor.setSummary(hexColor);
        }
        mNotificationBadgeTextColor.setNewPreviewColor(intColor);

        mNotificationBadgeTextSize = (SlimSeekBarPreference)
                findPreference(SettingsProvider.KEY_NOTIFICATION_BADGE_TEXT_SIZE);
        mNotificationBadgeTextSize.setInitValue((int) SettingsProvider.getFloat(mContext,
                SettingsProvider.KEY_NOTIFICATION_BADGE_TEXT_SIZE,
                getResources().getDimension(R.dimen.infomation_count_textsize)));
        mNotificationBadgeTextSize.setMax((int) TEXT_SIZE_MAX);
        mNotificationBadgeTextSize.setMin((int) TEXT_SIZE_MIN);
        mNotificationBadgeTextSize.setOnPreferenceChangeListener(this);


        mNotificationBadgeCornerRadius = (SlimSeekBarPreference)
                findPreference(SettingsProvider.KEY_NOTIFICATION_BADGE_CORNER_RADIUS);
        mNotificationBadgeCornerRadius.setInitValue((int) SettingsProvider.getFloat(mContext,
                SettingsProvider.KEY_NOTIFICATION_BADGE_CORNER_RADIUS,
                getResources().getDimension(R.dimen.infomation_count_circle_radius)));
        mNotificationBadgeCornerRadius.setMax((int) CORNER_RADIUS_MAX);
        mNotificationBadgeCornerRadius.setMin((int) CORNER_RADIUS_MIN);
        mNotificationBadgeCornerRadius.setOnPreferenceChangeListener(this);

        mNotificationBadgeExcludedApps = (AppMultiSelectListPreference)
                findPreference(SettingsProvider.KEY_NOTIFICATION_BADGE_EXCLUDED_APPS);
        Set<String> excludedApps = getExcludedApps();
        if (excludedApps != null) mNotificationBadgeExcludedApps.setValues(excludedApps);
        mNotificationBadgeExcludedApps.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNotificationBadgeColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            SettingsProvider.putInt(getActivity(),
                    SettingsProvider.KEY_NOTIFICATION_BADGE_COLOR, intHex);
            return true;
        } else if (preference == mNotificationBadgeTextColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            SettingsProvider.putInt(getActivity(),
                    SettingsProvider.KEY_NOTIFICATION_BADGE_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mNotificationBadgeTextSize) {
            float val = Float.parseFloat((String) newValue);
            SettingsProvider.putFloat(mContext,
                    SettingsProvider.KEY_NOTIFICATION_BADGE_TEXT_SIZE, val);
            return true;
        } else if (preference == mNotificationBadgeCornerRadius) {
            float val = Float.parseFloat((String) newValue);
            SettingsProvider.putFloat(mContext,
                    SettingsProvider.KEY_NOTIFICATION_BADGE_CORNER_RADIUS, val);
            return true;
        } else if (preference == mNotificationBadgeExcludedApps) {
            storeExcludedApps((Set<String>) newValue);
            return true;
        }
        return false;
    }

    private Set<String> getExcludedApps() {
        String excluded = SettingsProvider.getString(mContext,
                SettingsProvider.KEY_NOTIFICATION_BADGE_EXCLUDED_APPS, "");

        if (TextUtils.isEmpty(excluded)) return null;

        return new HashSet<String>(Arrays.asList(excluded.split("\\|")));
    }

    private void storeExcludedApps(Set<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        SettingsProvider.putString(mContext, SettingsProvider.KEY_NOTIFICATION_BADGE_EXCLUDED_APPS,
                builder.toString());
    }

	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        if (!NotificationListener.isEnabled(mContext)) {
            startNotificationListenerSettings();
        }
		SettingsProvider.putBoolean(mContext, SettingsProvider.KEY_NOTIFICATION_BADGES, isChecked);
	}

    private void startNotificationListenerSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.notification_badge_warning_message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(
                            new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    }
        });
        builder.show();
    }
}
