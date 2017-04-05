/*
 * Copyright (C) 2010 Daniel Nilsson
 * Copyright (C) 2013 Slimroms
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.margaritov.preference.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.android.launcher3.R;

@SuppressWarnings("WeakerAccess")
public class ColorPickerDialog
        extends
        Dialog
        implements
        ColorPickerView.OnColorChangedListener,
        View.OnClickListener {

    private ColorPickerView mColorPicker;

    private ColorPickerPanelView mOldColor;
    private ColorPickerPanelView mNewColor;

    private EditText mHex;

    private ImageButton mResetButton;
    private int mDefaultColor = -1;

    private OnColorChangedListener mListener;

    public ColorPickerDialog(Context context, int initialColor) {
        super(context);

        init(initialColor);
    }

    private void init(int color) {
        // To fight color branding.
        if (getWindow() != null) getWindow().setFormat(PixelFormat.RGBA_8888);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setUp(color);

    }

    private void setUp(int color) {
        View layout = View.inflate(getContext(), R.layout.dialog_color_picker, null);

        setContentView(layout);

        setTitle(R.string.dialog_color_picker);

        mColorPicker = (ColorPickerView) layout.findViewById(R.id.color_picker_view);
        mOldColor = (ColorPickerPanelView) layout.findViewById(R.id.old_color_panel);
        mNewColor = (ColorPickerPanelView) layout.findViewById(R.id.new_color_panel);

        ColorPickerPanelView p1 = (ColorPickerPanelView) layout.findViewById(R.id.white_panel);
        ColorPickerPanelView p2 = (ColorPickerPanelView) layout.findViewById(R.id.black_panel);
        ColorPickerPanelView p3 = (ColorPickerPanelView) layout.findViewById(R.id.cyan_panel);
        ColorPickerPanelView p4 = (ColorPickerPanelView) layout.findViewById(R.id.red_panel);
        ColorPickerPanelView p5 = (ColorPickerPanelView) layout.findViewById(R.id.green_panel);
        ColorPickerPanelView p6 = (ColorPickerPanelView) layout.findViewById(R.id.yellow_panel);

        mHex = (EditText) layout.findViewById(R.id.hex);
        ImageButton setButton = (ImageButton) layout.findViewById(R.id.enter);
        mResetButton = (ImageButton) layout.findViewById(R.id.reset);

        int foreground = getAttrColor(android.R.attr.colorForeground);
        mResetButton.setColorFilter(foreground);
        setButton.setColorFilter(foreground);


        ((LinearLayout) mOldColor.getParent()).setPadding(
                Math.round(mColorPicker.getDrawingOffset()),
                0,
                Math.round(mColorPicker.getDrawingOffset()),
                0
        );

        mOldColor.setOnClickListener(this);
        mNewColor.setOnClickListener(this);
        mColorPicker.setOnColorChangedListener(this);
        mOldColor.setColor(color);
        mColorPicker.setColor(color, true);

        setColorAndClickAction(p1, Color.WHITE);
        setColorAndClickAction(p2, Color.BLACK);
        setColorAndClickAction(p3, 0xff4285F4);
        setColorAndClickAction(p4, 0xffea4335);
        setColorAndClickAction(p5, 0xfffbbc05);
        setColorAndClickAction(p6, 0xff34a853);

        if (mHex != null) {
            mHex.setText(ColorPickerPreference.convertToARGB(color));
        }
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mHex.getText().toString();
                try {
                    int newColor = ColorPickerPreference.convertToColorInt(text);
                    mColorPicker.setColor(newColor, true);
                } catch (Exception e) {
                    // ignore
                }
            }
        });
        mResetButton.setVisibility(View.GONE);
    }

    private int getAttrColor(@AttrRes int attr) {
        TypedValue tv = new TypedValue();
        getContext().getTheme().resolveAttribute(attr, tv, true);
        return ContextCompat.getColor(getContext(), tv.resourceId);
    }

    @Override
    public void onColorChanged(int color) {
        mNewColor.setColor(color);
        try {
            if (mHex != null) {
                mHex.setText(ColorPickerPreference.convertToARGB(color));
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public void setDefaultColor(int color) {
        mDefaultColor = color;
        if (mResetButton != null) {
            mResetButton.setVisibility(View.VISIBLE);
            mResetButton.setOnClickListener(this);
        }
    }

    public void setAlphaSliderVisible(boolean visible) {
        mColorPicker.setAlphaSliderVisible(visible);
    }

    public void setColorAndClickAction(ColorPickerPanelView previewRect, final int color) {
        if (previewRect != null) {
            previewRect.setColor(color);
            previewRect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mColorPicker.setColor(color, true);
                    } catch (Exception ignore) {
                    }
                }
            });
        }
    }

    /**
     * Set a OnColorChangedListener to get notified when the color selected by the user has changed.
     */
    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    public int getColor() {
        return mColorPicker.getColor();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.new_color_panel) {
            if (mListener != null) {
                mListener.onColorChanged(mNewColor.getColor());
            }
            dismiss();
        } else if (v.getId() == R.id.reset) {
            try {
                mColorPicker.setColor(mDefaultColor, true);
            } catch (Exception ignored) {
            }
        }
    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt("old_color", mOldColor.getColor());
        state.putInt("new_color", mNewColor.getColor());
        return state;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mOldColor.setColor(savedInstanceState.getInt("old_color"));
        mColorPicker.setColor(savedInstanceState.getInt("new_color"), true);
    }

    public interface OnColorChangedListener {
        void onColorChanged(int color);
    }

}
