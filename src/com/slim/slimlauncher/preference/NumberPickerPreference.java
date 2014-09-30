package com.slim.slimlauncher.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.slim.slimlauncher.R;

public class NumberPickerPreference extends DialogPreference {
    private int mMin, mMax, mDefault;

    private String mMaxExternalKey, mMinExternalKey;
    private Preference mMaxExternalPreference, mMinExternalPreference;

    private boolean mEnabled;

    private NumberPicker mNumberPicker;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray prefType = context.obtainStyledAttributes(attrs,
                R.styleable.Preference, 0, 0);
        TypedArray numberPickerType = context.obtainStyledAttributes(attrs,
                R.styleable.NumberPickerPreference, 0, 0);

        mMaxExternalKey = numberPickerType.getString(R.styleable.NumberPickerPreference_maxExternal);
        mMinExternalKey = numberPickerType.getString(R.styleable.NumberPickerPreference_minExternal);

        mMax = prefType.getInt(R.styleable.Preference_max, 5);
        mMin = prefType.getInt(R.styleable.Preference_min, 0);

        mDefault = prefType.getInt(R.styleable.Preference_defaultValue, mMin);

        prefType.recycle();
        numberPickerType.recycle();
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.preference, null);

        TextView name = (TextView) view.findViewById(R.id.title);
        if (name != null && getTitle() != null) {
            name.setText(getTitle());
        }

        TextView summary = (TextView) view.findViewById(R.id.summary);
        if (summary != null && getSummary() != null) {
            summary.setText(getSummary());
        } else if (summary != null) {
            summary.setVisibility(View.GONE);
        }

        ImageView image = (ImageView) view.findViewById(R.id.icon);
        if (image.getDrawable() == null) {
            image.setVisibility(View.GONE);
        } else if (getIcon() != null) {
            image.setImageDrawable(getIcon());
        }

        return view;
    }

    protected void onAttachedToActivity() {
        // External values
        if (mMaxExternalKey != null) {
            Preference maxPreference = findPreferenceInHierarchy(mMaxExternalKey);
            if (maxPreference != null) {
                if (maxPreference instanceof NumberPickerPreference) {
                    mMaxExternalPreference = maxPreference;
                }
            }
        }
        if (mMinExternalKey != null) {
            Preference minPreference = findPreferenceInHierarchy(mMinExternalKey);
            if (minPreference != null) {
                if (minPreference instanceof NumberPickerPreference) {
                    mMinExternalPreference = minPreference;
                }
            }
        }
    }

    public void setDefaultValue(int defaultValue) {
        mDefault = defaultValue;
    }

    @Override
    protected View onCreateDialogView() {
        int max = mMax;
        int min = mMin;

        // External values
        if (mMaxExternalKey != null && mMaxExternalPreference != null) {
            max = mMaxExternalPreference.getSharedPreferences().getInt(mMaxExternalKey, mMax);
        }
        if (mMinExternalKey != null && mMinExternalPreference != null) {
            min = mMinExternalPreference.getSharedPreferences().getInt(mMinExternalKey, mMin);
        }

        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_dialog, null);

        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);

        // Initialize state
        mNumberPicker.setMaxValue(max);
        mNumberPicker.setMinValue(min);
        mNumberPicker.setValue(getPersistedInt(mDefault));
        mNumberPicker.setWrapSelectorWheel(false);
        mNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistInt(mNumberPicker.getValue());
        }
    }

}
