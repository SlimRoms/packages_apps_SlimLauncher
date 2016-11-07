package org.slim.launcher;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

import org.slim.launcher.util.AllAppsAnimation;
import org.slim.launcher.util.ColorUtils;
import org.slim.launcher.util.GestureHelper;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SlimLauncher extends Launcher {

    private static SlimLauncher sLauncher;

    private SlimDeviceProfile mSlimProfile;
    private GestureHelper mGestureHelper;
    private AllAppsAnimation mAllAppsAnimation;

    private SlimSearchBar mSearchBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        sLauncher = this;
        setLauncherCallbacks(new SlimLauncherCallbacks());
        super.onCreate(savedInstanceState);
        setInitialPreferences();

        mAllAppsAnimation = new AllAppsAnimation(this, getAppsView());

        mGestureHelper = new GestureHelper(this);
        getWorkspace().setWorkspaceCallbacks(new SlimWorkspaceCallbacks());

        /*getHotseat().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                getAppsView().setVisibility(View.VISIBLE);
                getAppsView().getContentView().setVisibility(View.VISIBLE);
                getAppsView().setY(motionEvent.getRawY());
                return true;
            }
        });*/
    }

    public boolean hotseatEvent(MotionEvent event) {
        return mAllAppsAnimation.hotseatTouchEvent(event);
    }

    public boolean allAppsEvent(MotionEvent event) {
        return mAllAppsAnimation.appDrawerTouchEvent(event);
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
        updateAppDrawerSearchBar();
        getAppsView().updateBackgroundAndPaddings();
    }

    public void preferenceChanged(String key) {
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
            case SettingsProvider.KEY_DRAWER_SEARCH_ENABLED:
                updateAppDrawerSearchBar();
                break;
            case SettingsProvider.KEY_DRAWER_BACKGROUND_COLOR:
            case SettingsProvider.KEY_DRAWER_DISABLE_CARD:
                getAppsView().updateBackgroundAndPaddings();
                mAllAppsAnimation.updateBackgroundColor();
                break;
            case SettingsProvider.KEY_HOTSEAT_BACKGROUND:
                mAllAppsAnimation.updateBackgroundColor();
                break;
        }
    }

    public void updateAppDrawerBackground() {
        if (getAppsView() == null) return;

        Drawable reveal = getAppsView().getRevealView().getBackground();
        Drawable content = getAppsView().getContentView().getBackground();

        if (reveal == null || content == null) {
            InsetDrawable background = new InsetDrawable(ContextCompat.getDrawable(this,
                    R.drawable.quantum_panel),
                    getAppsView().getContentPadding().left, 0,
                    getAppsView().getContentPadding().right, 0);
            reveal = background.getConstantState() != null ?
                    background.getConstantState().newDrawable() : background;
            content = background;
        }

        int color = SettingsProvider.getInt(this,
                SettingsProvider.KEY_DRAWER_BACKGROUND_COLOR,
                ContextCompat.getColor(this, R.color.quantum_panel_bg_color));
        boolean useCard = SettingsProvider.getBoolean(this,
                SettingsProvider.KEY_DRAWER_DISABLE_CARD, false);

        if (!useCard) {
            getAppsView().setBackgroundColor(color);
        } else {
            getAppsView().setBackgroundColor(Color.TRANSPARENT);
            reveal.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            content.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
        boolean lightStatusBar = ColorUtils.darkTextColor(color);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (lightStatusBar && !useCard) {
                getAppsView().getContentView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                getAppsView().getContentView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }*/

        getAppsView().getContentView().setBackground(useCard ? content : null);
        getAppsView().getRevealView().setBackground(useCard ? reveal : null);

        if (!useCard) {
            getAppsView().getAppsRecyclerView().setClipChildren(false);
            getAppsView().getAppsRecyclerView().setClipToPadding(false);
            getAppsView().getAppsRecyclerView().setPadding(
                    getAppsView().getAppsRecyclerView().getPaddingLeft(),
                    (getAppsView().getContentView().getPaddingTop() +
                            getAppsView().getPaddingTop()),
                    getAppsView().getAppsRecyclerView().getPaddingRight(),
                    (getAppsView().getContentView().getPaddingBottom() +
                            getAppsView().getPaddingBottom()));
            getAppsView().setPadding(0, 0, 0, 0);
        }

        mAllAppsAnimation.updateBackgroundColor();
    }

    @Override
    public boolean isAppsViewVisible() {
        Log.d("TEST", "appsView.y=" + getAppsView().getY());
        return getAppsView().getY() < 4 || getAppsView().getY() > -4;
    }

    @Override
    public void enterSpringLoadedDragMode() {
        if (isAppsViewVisible()) {
            mAllAppsAnimation.hideAllApps();
        }
        super.enterSpringLoadedDragMode();
    }

    private void updateAppDrawerSearchBar() {
        boolean searchEnabled = SettingsProvider.getBoolean(this,
                SettingsProvider.KEY_DRAWER_SEARCH_ENABLED, true);
        getAppsView().setSearchBarContainerViewVisibility(searchEnabled ? View.VISIBLE :View.GONE);
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
    public boolean onLongClick(View view) {
        return super.onLongClick(view);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (ShortcutHelper.ACTION_SLIM_LAUNCHER_SHORTCUT.equals(intent.getAction())) {
            onClickLauncherAction(null, intent);
            mOnResumeState = State.NONE;
            return;
        }
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        boolean alreadyOnHome = mHasFocus && ((intent.getFlags() &
                Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        if (alreadyOnHome) {
            mAllAppsAnimation.hideAllApps();
        }
        super.onNewIntent(intent);
    }

    @Override
    public void showAppsView(boolean animated, boolean resetListToTop, boolean updatePredictedApps,
                             boolean focusSearchBar) {
        mAllAppsAnimation.showAllApps();
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
            startGlobalSearch(initialQuery, selectInitialQuery,
                    appSearchData, sourceBounds);
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
                        getLayoutInflater().inflate(R.layout.search_bar_pixel, getSearchDropTargetBar(), false);
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
            if (getHotseat().getHeight() > event.getRawY()) {
                Log.d("TEST", "hotseat");
            }
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
