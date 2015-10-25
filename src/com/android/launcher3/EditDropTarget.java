/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.launcher3;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

public class EditDropTarget extends ButtonDropTarget {

    public EditDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mOriginalTextColor = getTextColors();

        // Get the hover color
        Resources r = getResources();
        mHoverColor = r.getColor(R.color.edit_target_hover_tint);

        setDrawable(R.drawable.edit_target_selector);
    }

    @Override
    protected boolean supportsDrop(DragSource source, Object info) {
        return supportsDrop(info);
    }

    public static boolean supportsDrop(Object info) {
        return info instanceof AppInfo;
    }

    @Override
    public void completeDrop(DragObject d) {
        mLauncher.updateShortcut((ItemInfo) d.dragInfo);
    }
}