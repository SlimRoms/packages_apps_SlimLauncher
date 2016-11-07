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
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import org.slim.launcher.SlimLauncher;

public class Hotseat extends FrameLayout
        implements Stats.LaunchSourceProvider{

    private CellLayout mContent;

    private Launcher mLauncher;

    private int mAllAppsButtonRank;

    private final boolean mHasVerticalHotseat;

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLauncher = (Launcher) context;
        mHasVerticalHotseat = mLauncher.getDeviceProfile().isVerticalBarLayout();
    }

    CellLayout getLayout() {
        return mContent;
    }

    /**
     * Registers the specified listener on the cell layout of the hotseat.
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mContent.setOnLongClickListener(l);
    }

    /* Get the orientation invariant order of the item in the hotseat for persistence. */
    int getOrderInHotseat(int x, int y) {
        return mHasVerticalHotseat ? (mContent.getCountY() - y - 1) : x;
    }

    /* Get the orientation specific coordinates given an invariant order in the hotseat. */
    int getCellXFromOrder(int rank) {
        return mHasVerticalHotseat ? 0 : rank;
    }

    int getCellYFromOrder(int rank) {
        return mHasVerticalHotseat ? (mContent.getCountY() - (rank + 1)) : 0;
    }

    public boolean isAllAppsButtonRank(int rank) {
        return rank == mAllAppsButtonRank;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        DeviceProfile grid = mLauncher.getDeviceProfile();

        mAllAppsButtonRank = grid.inv.hotseatAllAppsRank;
        mContent = (CellLayout) findViewById(R.id.layout);
        if (grid.isLandscape && !grid.isLargeTablet) {
            mContent.setGridSize(1, grid.inv.numHotseatIcons);
        } else {
            mContent.setGridSize(grid.inv.numHotseatIcons, 1);
        }
        mContent.setIsHotseat(true);

        /*mContent.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return SlimLauncher.getInstance().hotseatEvent(motionEvent);
            }
        });*/

        resetLayout();
    }

    void resetLayout() {
        mContent.removeAllViewsInLayout();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // We don't want any clicks to go through to the hotseat unless the workspace is in
        // the normal state.
        return mLauncher.getWorkspace().workspaceInModalState()
                || SlimLauncher.getInstance().hotseatEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return SlimLauncher.getInstance().hotseatEvent(e) || super.onTouchEvent(e);
    }

    @Override
    public void fillInLaunchSourceData(View v, Bundle sourceData) {
        sourceData.putString(Stats.SOURCE_EXTRA_CONTAINER, Stats.CONTAINER_HOTSEAT);
    }

    public CellLayout getContent() {
        return mContent;
    }
}
