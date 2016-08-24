package org.slim.launcher;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.AppInfo;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Workspace;
import com.android.launcher3.WorkspaceCallbacks;
import com.android.launcher3.allapps.AllAppsSearchBarController;
import org.slim.launcher.settings.SettingsActivity;
import org.slim.launcher.settings.SettingsProvider;
import com.android.launcher3.util.ComponentKey;

import org.slim.launcher.util.GestureHelper;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SlimLauncher extends Launcher {

    private static SlimLauncher sLauncher;

    private SlimDeviceProfile mSlimProfile;
    private GestureHelper mGestureHelper;

    private SlimSearchBar mSearchBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        sLauncher = this;
        setLauncherCallbacks(new SlimLauncherCallbacks());
        super.onCreate(savedInstanceState);
        setInitialPreferences();

        mGestureHelper = new GestureHelper(this);
        getWorkspace().setWorkspaceCallbacks(new SlimWorkspaceCallbacks());
    }

    public SlimDeviceProfile getSlimDeviceProfile() {
        return mSlimProfile;
    }

    public void updateDynamicGrid() {
        mSlimProfile.updateFromPreferences();
        getDeviceProfile().layout(this);
    }

    void setInitialPreferences() {
        updateDynamicGrid();
        updateWorkspaceGridSize();
        updateHotseatCellCount();
        updateSearchBarVisibility();
    }

    public void preferenceChanged(String key) {
        Log.d("TEST", "key=" + key);
        mGestureHelper.updateGestures();
        updateDynamicGrid();
        mSlimProfile.updateFromPreferences();
        switch (key) {
            case SettingsProvider.KEY_HOMESCREEN_GRID:
                updateWorkspaceGridSize();
                break;
            case SettingsProvider.KEY_DOCK_ICONS:
                updateHotseatCellCount();
                break;
            case SettingsProvider.KEY_SHOW_SEARCH_BAR:
                updateSearchBarVisibility();
                break;
        }
    }

    private void updateWorkspaceGridSize() {
        InvariantDeviceProfile inv = LauncherAppState.getInstance().getInvariantDeviceProfile();
        int rows = SettingsProvider.getCellCountY(SlimLauncher.this,
                SettingsProvider.KEY_HOMESCREEN_GRID, inv.numRows);
        int cols = SettingsProvider.getCellCountX(SlimLauncher.this,
                SettingsProvider.KEY_HOMESCREEN_GRID, inv.numColumns);
        inv.numRows = rows;
        inv.numColumns = cols;
        Workspace w = getWorkspace();
        for (int i = 0; i < w.getChildCount(); i++) {
            ((CellLayout) w.getChildAt(i)).setGridSize(cols, rows);
        }
    }

    @Override
    public boolean isAllAppsButtonRank(int rank) {
        return false;
    }

    @Override
    public View createShortcut(ViewGroup parent, ShortcutInfo info) {
        View favorite = super.createShortcut(parent, info);
        if (info.getIntent().getAction().equals(ShortcutHelper.ACTION_SLIM_LAUNCHER_SHORTCUT)) {
            info.launcherAction = true;
        }
        return favorite;
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
            ShortcutInfo info = (ShortcutInfo) tag;
            if (info.launcherAction) {
                onClickLauncherAction(v, info.getIntent());
                return;
            }
        }
        super.onClick(v);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (ShortcutHelper.ACTION_SLIM_LAUNCHER_SHORTCUT.equals(intent.getAction())) {
            onClickLauncherAction(null, intent);
            mOnResumeState = State.NONE;
            return;
        }
        super.onNewIntent(intent);
    }

    public void onClickLauncherAction(View view, Intent intent) {
        String value = intent.getStringExtra(ShortcutHelper.SHORTCUT_VALUE);
        switch (value) {
            case ShortcutHelper.SHORTCUT_ALL_APPS:
                setAllAppsButton(view);
                if (isAppsViewVisible()) {
                    showWorkspace(true);
                } else {
                    onClickAllAppsButton(view);
                }
                break;
            case ShortcutHelper.SHORTCUT_OVERVIEW:
                if (getWorkspace().isInOverviewMode()) {
                    showWorkspace(true);
                } else {
                    showOverviewMode(true);
                }
                break;
            case ShortcutHelper.SHORTCUT_SETTINGS:
                onClickSettingsButton(view);
                break;
            case ShortcutHelper.SHORTCUT_DEFAULT_PAGE:
                getWorkspace().moveToDefaultScreen(true);
                break;
        }
    }

    private void updateSearchBarVisibility() {
        mSearchBar.updateSearchBarVisibility();
    }

    private void updateHotseatCellCount() {
        DeviceProfile grid = getDeviceProfile();
        grid.inv.numHotseatIcons = SettingsProvider.getInt(SlimLauncher.this,
                SettingsProvider.KEY_DOCK_ICONS, grid.inv.numHotseatIcons);
        if (grid.isLandscape && !grid.isLargeTablet) {
            ((CellLayout) findViewById(R.id.layout))
                    .setGridSize(1, grid.inv.numHotseatIcons);
        } else {
            ((CellLayout) findViewById(R.id.layout))
                    .setGridSize(grid.inv.numHotseatIcons, 1);
        }
    }

    public static SlimLauncher getInstance() {
        return sLauncher;
    }

    private class SlimLauncherCallbacks implements LauncherCallbacks {

        @Override
        public void preOnCreate() {
            mSlimProfile = new SlimDeviceProfile(SlimLauncher.this);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            sLauncher = SlimLauncher.this;
        }

        @Override
        public void preOnResume() {
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onStop() {
        }

        @Override
        public void onPause() {
        }

        @Override
        public void onDestroy() {
            sLauncher = null;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
        }

        @Override
        public void onPostCreate(Bundle savedInstanceState) {
        }

        @Override
        public void onNewIntent(Intent intent) {
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
        }

        @Override
        public void onAttachedToWindow() {
        }

        @Override
        public void onDetachedFromWindow() {
        }

        @Override
        public boolean onPrepareOptionsMenu(Menu menu) {
            return false;
        }

        @Override
        public void dump(String prefix, FileDescriptor fd, PrintWriter w, String[] args) {
        }

        @Override
        public void onHomeIntent() {
        }

        @Override
        public boolean handleBackPressed() {
            return false;
        }

        @Override
        public void onTrimMemory(int level) {
        }

        @Override
        public void onLauncherProviderChange() {
        }

        @Override
        public void finishBindingItems(boolean upgradePath) {
        }

        @Override
        public void onClickAllAppsButton(View v) {
        }

        @Override
        public void bindAllApplications(ArrayList<AppInfo> apps) {
        }

        @Override
        public void onClickFolderIcon(View v) {
        }

        @Override
        public void onClickAppShortcut(View v) {
        }

        @Override
        public void onClickPagedViewIcon(View v) {
        }

        @Override
        public void onClickWallpaperPicker(View v) {
        }

        @Override
        public void onClickSettingsButton(View v) {
            Intent i = new Intent(SlimLauncher.this, SettingsActivity.class);
            startActivity(i);
        }

        @Override
        public void onClickAddWidgetButton(View v) {
        }

        @Override
        public void onPageSwitch(View newPage, int newPageIndex) {
        }

        @Override
        public void onWorkspaceLockedChanged() {
        }

        @Override
        public void onDragStarted(View view) {
        }

        @Override
        public void onInteractionBegin() {
        }

        @Override
        public void onInteractionEnd() {
        }

        @Override
        public boolean providesSearch() {
            return true;
        }

        @Override
        public boolean startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
            return false;
        }

        @Override
        public boolean startSearchFromAllApps(String query) {
            return false;
        }

        @Override
        public boolean hasCustomContentToLeft() {
            return false;
        }

        @Override
        public void populateCustomContentContainer() {
        }

        @Override
        public View getQsbBar() {
            Log.d("TEST", "getQsbBar");
            if (mSearchBar == null) {
                mSearchBar = (SlimSearchBar)
                        getLayoutInflater().inflate(R.layout.qsb, getSearchDropTargetBar(), false);
                mSearchBar.setLauncher(SlimLauncher.this);
                getSearchDropTargetBar().addView(mSearchBar);
            }
            return mSearchBar;
        }

        @Override
        public Bundle getAdditionalSearchWidgetOptions() {
            return new Bundle();
        }

        @Override
        public Intent getFirstRunActivity() {
            return null;
        }

        @Override
        public boolean hasFirstRunActivity() {
            return false;
        }

        @Override
        public boolean hasDismissableIntroScreen() {
            return false;
        }

        @Override
        public View getIntroScreen() {
            return null;
        }

        @Override
        public boolean shouldMoveToDefaultScreenOnHomeIntent() {
            return false;
        }

        @Override
        public boolean hasSettings() {
            return true;
        }

        @Override
        public boolean overrideWallpaperDimensions() {
            return false;
        }

        @Override
        public boolean isLauncherPreinstalled() {
            return SlimLauncher.this.isLauncherPreinstalled();
        }

        @Override
        public AllAppsSearchBarController getAllAppsSearchBarController() {
            return null;
        }

        @Override
        public List<ComponentKey> getPredictedApps() {
            return null;
        }

        @Override
        public int getSearchBarHeight() {
            return SEARCH_BAR_HEIGHT_NORMAL;
        }

        @Override
        public void setLauncherSearchCallback(Object callbacks) {
        }
    }

    public class SlimWorkspaceCallbacks implements WorkspaceCallbacks {

        public GestureDetector mGestureDetector;

        private float mInitialDistance;

        public SlimWorkspaceCallbacks() {
            mGestureDetector = new GestureDetector(SlimLauncher.this,
                    new WorkspaceGestureListener());
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            //Log.d("TEST", "pointers=" + event.getPointerCount());
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (event.getPointerCount() > 1) {
                        mInitialDistance = getSpacing(event);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() > 1) {
                        float newDistance = getSpacing(event);
                        //Log.d("TEST", "space=" + newDistance);
                        float scale = newDistance / mInitialDistance;
                        //Log.d("TEST", "scale=" + scale);
                        if (scale < 0.5) {
                            mGestureHelper.handleGestureAction(GestureHelper.Gesture.PINCH);
                        } else if (scale > 2.2){
                            mGestureHelper.handleGestureAction(GestureHelper.Gesture.SPREAD);
                        }
                    }
                    break;
            }
            return mGestureDetector.onTouchEvent(event);
        }

        private float getSpacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        }

        class WorkspaceGestureListener extends GestureDetector.SimpleOnGestureListener {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mGestureHelper.handleGestureAction(GestureHelper.Gesture.DOUBLE_TAP);
                return true;
            }

            @Override
            public boolean onFling(MotionEvent start, MotionEvent finish,
                                   float xVelocity, float yVelocity) {
                return mGestureHelper.handleFling(start, finish);
            }
        }
    }
}
