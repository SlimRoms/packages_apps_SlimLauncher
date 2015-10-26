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

package com.android.launcher3.allapps.paged;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import com.android.launcher3.AppInfo;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DeleteDropTarget;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DragSource;
import com.android.launcher3.DropTarget;
import com.android.launcher3.Folder;
import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAnimUtils;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherTransitionable;
import com.android.launcher3.PagedView;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutAndWidgetContainer;
import com.android.launcher3.Workspace;
import com.android.launcher3.model.AppNameComparator;
import com.android.launcher3.settings.SettingsProvider;
import com.android.launcher3.util.GestureHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * The Apps/Customize page that displays all the applications, widgets, and shortcuts.
 */
public class AppsCustomizePagedView extends PagedView implements
        View.OnClickListener, View.OnLongClickListener, View.OnKeyListener, View.OnTouchListener,
        DragSource, LauncherTransitionable {

    /*
     * We load an extra page on each side to prevent flashes from scrolling and loading of the
     * widget previews in the background with the AsyncTasks.
     */
    final static int sLookBehindPageCount = 2;
    final static int sLookAheadPageCount = 2;
    private final LayoutInflater mLayoutInflater;

    // Previews & outlines
    boolean mPageBackgroundsVisible = true;
    private ContentType mContentType = ContentType.Applications;
    private SortMode mSortMode = SortMode.Title;

    // Refs
    private Launcher mLauncher;

    // Save and Restore
    private int mSaveInstanceStateItemIndex = -1;

    // Content
    private ArrayList<AppInfo> mApps;
    private ArrayList<AppInfo> mFilteredApps;
    private ArrayList<ComponentName> mHiddenApps;
    private GestureDetector mGestureDetector;

    // Dimens
    private int mContentWidth, mContentHeight;
    private int mNumAppsPages;
    Runnable mLayoutRunnable = new Runnable() {
        @Override
        public void run() {
            boolean attached = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                attached = isAttachedToWindow();
            }
            if (attached) {
                setDataIsReady();
                onDataReady();
            }
        }
    };
    private boolean mInBulkBind;
    private boolean mNeedToUpdatePageCountsAndInvalidateData;
    private boolean mIsDragging;

    public AppsCustomizePagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLayoutInflater = LayoutInflater.from(context);
        mApps = new ArrayList<>();
        mFilteredApps = new ArrayList<>();

        // Save the default widget preview background
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.AppsCustomizePagedView, 0, 0);
        a.recycle();

        // The padding on the non-matched dimension for the default widget preview icons
        // (top + bottom)
        mFadeInAdjacentScreens = false;

        // Unless otherwise specified this view is important for accessibility.
        if (getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        setSinglePageInViewport();
        setOverscrollUseBounce();

        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent start, MotionEvent finish,
                                           float xVelocity, float yVelocity) {
                        if (GestureHelper.isSwipeUP(finish.getRawY(), start.getRawY())) {
                            if (GestureHelper.doesSwipeUpContainShowAllApps(mLauncher)) {
                                mLauncher.showWorkspace(true);
                            }
                        } else if (GestureHelper.isSwipeDOWN(finish.getRawY(), start.getRawY())) {
                            if (GestureHelper.doesSwipeDownContainShowAllApps(mLauncher)) {
                                mLauncher.showWorkspace(true);
                            }
                        }
                        return true;
                    }
                }
        );

        updateSortMode(context);
        updateHiddenAppsList(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        handleTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    private void handleTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                cancelDragging();
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        handleTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        return false;
    }

    @Override
    public void getEdgeVerticalPostion(int[] pos) {
    }

    private void updateHiddenAppsList(Context context) {
        String[] flattened = SettingsProvider.getString(context,
                SettingsProvider.KEY_HIDDEN_APPS, "").split("\\|");
        mHiddenApps = new ArrayList<>(flattened.length);
        for (String flat : flattened) {
            ComponentName cmp = ComponentName.unflattenFromString(flat);
            if (cmp != null) {
                mHiddenApps.add(cmp);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        mCenterPagesVertically = false;
    }

    public void onFinishInflate() {
        super.onFinishInflate();

        LauncherAppState app = LauncherAppState.getInstance();
        InvariantDeviceProfile idp = app.getInvariantDeviceProfile();
        DeviceProfile grid = idp.portraitProfile;
        setPadding(grid.edgeMarginPx, 2 * grid.edgeMarginPx,
                grid.edgeMarginPx, 2 * grid.edgeMarginPx);
    }

    /**
     * Returns the item index of the center item on this page so that we can restore to this
     * item index when we rotate.
     */
    private int getMiddleComponentIndexOnCurrentPage() {
        int i = -1;
        if (getPageCount() > 0) {
            int currentPage = getCurrentPage();
            if (mContentType == ContentType.Applications) {
                AppsCustomizeCellLayout layout = (AppsCustomizeCellLayout) getPageAt(currentPage);
                ShortcutAndWidgetContainer childrenLayout = layout.getShortcutsAndWidgets();
                int numItemsPerPage = mCellCountX * mCellCountY;
                int childCount = childrenLayout.getChildCount();
                if (childCount > 0) {
                    i = (currentPage * numItemsPerPage) + (childCount / 2);
                }
            } else {
                throw new RuntimeException("Invalid ContentType");
            }
        }
        return i;
    }

    /**
     * Get the index of the item to restore to if we need to restore the current page.
     */
    public int getSaveInstanceStateIndex() {
        if (mSaveInstanceStateItemIndex == -1) {
            mSaveInstanceStateItemIndex = getMiddleComponentIndexOnCurrentPage();
        }
        return mSaveInstanceStateItemIndex;
    }

    /**
     * Returns the page in the current orientation which is expected to contain the specified
     * item index.
     */
    int getPageForComponent(int index) {
        if (index < 0) return 0;
        int numItemsPerPage = mCellCountX * mCellCountY;
        return (index / numItemsPerPage);
    }

    /**
     * Restores the page for an item at the specified index
     */
    public void restorePageForIndex(int index) {
        if (index < 0) return;
        mSaveInstanceStateItemIndex = index;
    }

    private void updatePageCounts() {
        mNumAppsPages = (int) Math.ceil((float) mFilteredApps.size() / (mCellCountX * mCellCountY));
    }

    public void filterContent() {
        filterAppsWithoutInvalidate();
    }

    public void updateGridSize() {
        DeviceProfile grid = mLauncher.getDeviceProfile();
        mCellCountX = grid.pagedAllAppsNumCols;
        mCellCountY = grid.pagedAllAppsNumRows;
        updatePageCounts();
        updateSortMode(mLauncher);
    }

    public void updateSortMode(Context context) {
        int sortMode = Integer.parseInt(SettingsProvider.getString(context,
                SettingsProvider.KEY_DRAWER_SORT_MODE, "0"));
        if (sortMode == 0) {
            mSortMode = SortMode.Title;
        } else if (sortMode == 1) {
            mSortMode = SortMode.LaunchCount;
        } else if (sortMode == 2) {
            mSortMode = SortMode.InstallTime;
        }
    }

    protected void onDataReady() {
        // Now that the data is ready, we can calculate the content width, the number of cells to
        // use for each page
        updateGridSize();

        // Force a measure to update recalculate the gaps
        mContentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        mContentHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

        final boolean hostIsTransitioning = getTabHost().isInTransition();
        int page = getPageForComponent(mSaveInstanceStateItemIndex);
        invalidatePageData(Math.max(0, page), hostIsTransitioning);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (!isDataReady()) {
            if (!mFilteredApps.isEmpty()) {
                post(mLayoutRunnable);
            }
        }
    }

    public void setBulkBind(boolean bulkBind) {
        if (bulkBind) {
            mInBulkBind = true;
        } else {
            mInBulkBind = false;
            if (mNeedToUpdatePageCountsAndInvalidateData) {
                updatePageCountsAndInvalidateData();
            }
        }
    }

    private void updatePageCountsAndInvalidateData() {
        if (mInBulkBind) {
            mNeedToUpdatePageCountsAndInvalidateData = true;
        } else {
            updatePageCounts();
            invalidateOnDataChange();
            mNeedToUpdatePageCountsAndInvalidateData = false;
        }
    }

    @Override
    public void onClick(View v) {
        // When we have exited all apps or are in transition, disregard clicks
        if (!mLauncher.isAllAppsVisible()
                || mLauncher.getWorkspace().isSwitchingState()) return;

        // Create a little animation to show that the widget can move
        float offsetY = getResources().getDimensionPixelSize(R.dimen.dragViewOffsetY);
        final ImageView p = (ImageView) v.findViewById(R.id.widget_preview);
        AnimatorSet bounce = LauncherAnimUtils.createAnimatorSet();
        ValueAnimator tyuAnim = LauncherAnimUtils.ofFloat(p, "translationY", offsetY);
        tyuAnim.setDuration(125);
        ValueAnimator tydAnim = LauncherAnimUtils.ofFloat(p, "translationY", 0f);
        tydAnim.setDuration(100);
        bounce.play(tyuAnim).before(tydAnim);
        bounce.setInterpolator(new AccelerateInterpolator());
        bounce.start();
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    private void beginDraggingApplication(View v) {
        mLauncher.getWorkspace().beginExternalDragShared(v, this);
    }

    protected boolean beginDragging(final View v) {
        if (!shouldBeginDragging()) return false;

        if (v instanceof BubbleTextView) {
            beginDraggingApplication(v);
        }

        // We delay entering spring-loaded mode slightly to make sure the UI
        // thready is free of any work.
        postDelayed(new Runnable() {
            @Override
            public void run() {
                // We don't enter spring-loaded mode if the drag has been cancelled
                if (mLauncher.getDragController().isDragging()) {
                    // Go into spring loaded mode (must happen before we startDrag())
                    mLauncher.enterSpringLoadedDragMode();
                }
            }
        }, 150);

        return true;
    }

    protected boolean shouldBeginDragging() {
        boolean wasDragging = mIsDragging;
        mIsDragging = true;
        return !wasDragging;
    }

    protected void cancelDragging() {
        mIsDragging = false;
    }

    /**
     * Clean up after dragging.
     *
     * @param target where the item was dragged to (can be null if the item was flung)
     */
    private void endDragging(View target, boolean isFlingToDelete, boolean success) {
        if (isFlingToDelete || !success || (target != mLauncher.getWorkspace() &&
                !(target instanceof DeleteDropTarget) && !(target instanceof Folder))) {
            // Exit spring loaded mode if we have not successfully dropped or have not handled the
            // drop in Workspace
            mLauncher.exitSpringLoadedDragModeDelayed(true,
                    Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
            mLauncher.unlockScreenOrientation(false);
        } else {
            mLauncher.unlockScreenOrientation(false);
        }
    }

    //@Override
    public View getContent() {
        if (getChildCount() > 0) {
            return getChildAt(0);
        }
        return null;
    }

    @Override
    public void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace) {
    }

    @Override
    public void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace) {
    }

    @Override
    public void onLauncherTransitionStep(Launcher l, float t) {
    }

    @Override
    public void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace) {
    }

    @Override
    public void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete,
                                boolean success) {
        // Return early and wait for onFlingToDeleteCompleted if this was the result of a fling
        if (isFlingToDelete) return;

        endDragging(target, false, success);

        // Display an error message if the drag failed due to there not being enough space on the
        // target layout we were dropping on.
        if (!success) {
            boolean showOutOfSpaceMessage = false;
            if (target instanceof Workspace) {
                int currentScreen = mLauncher.getCurrentWorkspaceScreen();
                Workspace workspace = (Workspace) target;
                CellLayout layout = (CellLayout) workspace.getChildAt(currentScreen);
                ItemInfo itemInfo = (ItemInfo) d.dragInfo;
                if (layout != null) {
                    layout.calculateSpans(itemInfo);
                    showOutOfSpaceMessage =
                            !layout.findCellForSpan(null, itemInfo.spanX, itemInfo.spanY);
                }
            }
            if (showOutOfSpaceMessage) {
                mLauncher.showOutOfSpaceMessage(false);
            }

            d.deferDragViewCleanupPostAnimation = false;
        }
    }

    @Override
    public void onFlingToDeleteCompleted() {
        // We just dismiss the drag when we fling, so cleanup here
        endDragging(null, true, true);
    }

    @Override
    public boolean supportsFlingToDelete() {
        return true;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return true;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        DeviceProfile grid = mLauncher.getDeviceProfile();
        return (float) grid.allAppsIconSizePx / grid.iconSizePx;
    }

    public void setContentType(ContentType type) {
        // Widgets appear to be cleared every time you leave, always force invalidate for them
        if (mContentType != type) {
            mContentType = type;
            invalidatePageData(0, true);
        }
    }

    /*
     * Apps PagedView implementation
     */
    private void setVisibilityOnChildren(ViewGroup layout, int visibility) {
        int childCount = layout.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            layout.getChildAt(i).setVisibility(visibility);
        }
    }

    private void setupPage(AppsCustomizeCellLayout layout) {
        layout.setGridSize(mCellCountX, mCellCountY);

        // Note: We force a measure here to get around the fact that when we do layout calculations
        // immediately after syncing, we don't have a proper width.  That said, we already know the
        // expected page width, so we can actually optimize by hiding all the TextView-based
        // children that are expensive to measure, and let that happen naturally later.
        setVisibilityOnChildren(layout, View.GONE);
        int widthSpec = MeasureSpec.makeMeasureSpec(mContentWidth, MeasureSpec.AT_MOST);
        int heightSpec = MeasureSpec.makeMeasureSpec(mContentHeight, MeasureSpec.AT_MOST);
        layout.measure(widthSpec, heightSpec);

        Drawable bg = getContext().getResources().getDrawable(R.drawable.quantum_panel);
        //int color = SettingsProvider.getInt(mLauncher, SettingsProvider.KEY_DRAWER_BACKGROUND,
        //      Color.WHITE);
        if (bg != null) {
            bg.setAlpha(mPageBackgroundsVisible ? 255 : 0);
            //bg.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            layout.setBackground(bg);
        }

        setVisibilityOnChildren(layout, View.VISIBLE);
    }

    public void syncAppsPageItems(int page) {
        // ensure that we have the right number of items on the pages
        int numCells = mCellCountX * mCellCountY;
        int startIndex = page * numCells;
        int endIndex = Math.min(startIndex + numCells, mFilteredApps.size());
        AppsCustomizeCellLayout layout = (AppsCustomizeCellLayout) getPageAt(page);

        layout.removeAllViewsOnPage();
        //boolean hideIconLabels = SettingsProvider.getBoolean(mLauncher,
        //      SettingsProvider.KEY_DRAWER_HIDE_LABELS, false);
        for (int i = startIndex; i < endIndex; ++i) {
            AppInfo info = mFilteredApps.get(i);
            BubbleTextView icon = (BubbleTextView) mLayoutInflater.inflate(
                    R.layout.apps_customize_application, layout, false);
            /*if (!ColorUtils.darkTextColor(SettingsProvider.getInt(mLauncher,
                    SettingsProvider.KEY_DRAWER_BACKGROUND, Color.WHITE))) {
                icon.setTextColor(Color.WHITE);
            } else {
                icon.setTextColor(Color.BLACK);
            }*/
            icon.applyFromApplicationInfo(info);
            //icon.setTextVisibility(!hideIconLabels);
            icon.setOnClickListener(mLauncher);
            icon.setOnLongClickListener(this);
            icon.setOnTouchListener(this);
            icon.setOnKeyListener(this);
            icon.setOnFocusChangeListener(layout.mFocusHandlerView);

            int index = i - startIndex;
            int x = index % mCellCountX;
            int y = index / mCellCountX;
            layout.addViewToCellLayout(icon, -1, i, new CellLayout.LayoutParams(x, y, 1, 1), false);
        }

        enableHwLayersOnVisiblePages();
    }

    @Override
    public void syncPages() {
        removeAllViews();

        Context context = getContext();
        if (mContentType == ContentType.Applications) {
            for (int i = 0; i < mNumAppsPages; ++i) {
                AppsCustomizeCellLayout layout = new AppsCustomizeCellLayout(context);
                setupPage(layout);
                addView(layout, new PagedView.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT));
            }
        } else {
            throw new RuntimeException("Invalid ContentType");
        }

    }

    @Override
    public void syncPageItems(int page, boolean immediate) {
        syncAppsPageItems(page);
    }

    // We want our pages to be z-ordered such that the further a page is to the left, the higher
    // it is in the z-order. This is important to insure touch events are handled correctly.
    public View getPageAt(int index) {
        return getChildAt(indexToPage(index));
    }

    @Override
    protected int indexToPage(int index) {
        return getChildCount() - index - 1;
    }

    // In apps customize, we have a scrolling effect which emulates pulling cards off of a stack.
    @Override
    protected void screenScrolled(int screenCenter) {
        super.screenScrolled(screenCenter);
        enableHwLayersOnVisiblePages();
    }

    private void enableHwLayersOnVisiblePages() {
        final int screenCount = getChildCount();

        getVisiblePages(mTempVisiblePagesRange);
        int leftScreen = mTempVisiblePagesRange[0];
        int rightScreen = mTempVisiblePagesRange[1];
        int forceDrawScreen = -1;
        if (leftScreen == rightScreen) {
            // make sure we're caching at least two pages always
            if (rightScreen < screenCount - 1) {
                rightScreen++;
                forceDrawScreen = rightScreen;
            } else if (leftScreen > 0) {
                leftScreen--;
                forceDrawScreen = leftScreen;
            }
        } else {
            forceDrawScreen = leftScreen + 1;
        }

        for (int i = 0; i < screenCount; i++) {
            final View layout = getPageAt(i);
            if (!(leftScreen <= i && i <= rightScreen &&
                    (i == forceDrawScreen || shouldDrawChild(layout)))) {
                layout.setLayerType(LAYER_TYPE_NONE, null);
            }
        }

        for (int i = 0; i < screenCount; i++) {
            final View layout = getPageAt(i);

            if (leftScreen <= i && i <= rightScreen &&
                    (i == forceDrawScreen || shouldDrawChild(layout))) {
                if (layout.getLayerType() != LAYER_TYPE_HARDWARE) {
                    layout.setLayerType(LAYER_TYPE_HARDWARE, null);
                }
            }
        }
    }

    protected void overScroll(float amount) {
        dampedOverScroll(amount);
    }

    @Override
    public boolean onLongClick(View v) {
        /*if (mLauncher.getLockWorkspace()) {
            Toast.makeText(mLauncher,
                    mLauncher.getString(R.string.workspace_locked), Toast.LENGTH_SHORT).show();
            return true;
        }*/
        // Return early if this is not initiated from a touch
        if (!v.isInTouchMode()) return false;
        // Return early if we are still animating the pages
        if (mNextPage != INVALID_PAGE) return false;
        // When we have exited all apps or are in transition, disregard long clicks
        if (!mLauncher.isAllAppsVisible() ||
                mLauncher.getWorkspace().isSwitchingState()) return false;
        // Return if global dragging is not enabled

        return !mLauncher.isDraggingEnabled() && beginDragging(v);
    }

    /*
     * Determines if we should change the touch state to start scrolling after the
     * user moves their touch point too far.
     */
    protected void determineScrollingStart(MotionEvent ev) {
        if (!mIsDragging) super.determineScrollingStart(ev);
    }

    @Override
    protected void onPageEndMoving() {
        super.onPageEndMoving();
        mForceDrawAllChildrenNextFrame = true;
        // We reset the save index when we change pages so that it will be recalculated on next
        // rotation
        mSaveInstanceStateItemIndex = -1;
    }

    public Comparator<AppInfo> getComparatorForSortMode() {
        switch (mSortMode) {
            case Title:
                return AppNameComparator.getAppNameComparator();
            case LaunchCount:
                return AppNameComparator.getLaunchCountComparator(mLauncher.getStats());
            case InstallTime:
                return AppNameComparator.getAppInstallTimeComparator();
            default:
                return AppNameComparator.getAppNameComparator();
        }
    }

    /*
     * AllAppsView implementation
     */
    public void setup(Launcher launcher) {
        mLauncher = launcher;
    }

    /**
     * We should call thise method whenever the core data changes (mApps, mWidgets) so that we can
     * appropriately determine when to invalidate the PagedView page data.  In cases where the data
     * has yet to be set, we can requestLayout() and wait for onDataReady() to be called in the
     * next onMeasure() pass, which will trigger an invalidatePageData() itself.
     */
    public void invalidateOnDataChange() {
        if (!isDataReady()) {
            // The next layout pass will trigger data-ready if both widgets and apps are set, so
            // request a layout to trigger the page data when ready.
            requestLayout();
        } else {
            invalidatePageData();
        }
    }

    public void setApps(ArrayList<AppInfo> list) {
        mApps = list;
        filterAppsWithoutInvalidate();
        updatePageCountsAndInvalidateData();
    }

    private void addAppsWithoutInvalidate(ArrayList<AppInfo> list) {
        // We add it in place, in alphabetical order
        int count = list.size();
        for (int i = 0; i < count; ++i) {
            AppInfo info = list.get(i);
            int index = Collections.binarySearch(mApps, info, getComparatorForSortMode());
            if (index < 0) {
                mApps.add(-(index + 1), info);
            }
        }
    }

    public void addApps(ArrayList<AppInfo> list) {
        addAppsWithoutInvalidate(list);
        filterAppsWithoutInvalidate();
        updatePageCountsAndInvalidateData();
    }

    private int findAppByComponent(List<AppInfo> list, AppInfo item) {
        ComponentName removeComponent = item.intent.getComponent();
        int length = list.size();
        for (int i = 0; i < length; ++i) {
            AppInfo info = list.get(i);
            if (info.user.equals(item.user)
                    && info.intent.getComponent().equals(removeComponent)) {
                return i;
            }
        }
        return -1;
    }

    private void removeAppsWithoutInvalidate(ArrayList<AppInfo> list) {
        // loop through all the apps and remove apps that have the same component
        int length = list.size();
        for (int i = 0; i < length; ++i) {
            AppInfo info = list.get(i);
            int removeIndex = findAppByComponent(mApps, info);
            if (removeIndex > -1) {
                mApps.remove(removeIndex);
            }
        }
    }

    public void removeApps(ArrayList<AppInfo> appInfos) {
        removeAppsWithoutInvalidate(appInfos);
        filterAppsWithoutInvalidate();
        updatePageCountsAndInvalidateData();
    }

    public void filterAppsWithoutInvalidate() {
        updateHiddenAppsList(mLauncher);

        mFilteredApps = new ArrayList<>(mApps);
        Iterator<AppInfo> iterator = mFilteredApps.iterator();
        while (iterator.hasNext()) {
            AppInfo appInfo = iterator.next();
            if (mHiddenApps.contains(appInfo.componentName)) {
                iterator.remove();
            }
        }
        Collections.sort(mFilteredApps, getComparatorForSortMode());
    }

    public void reset() {
        // If we have reset, then we should not continue to restore the previous state
        mSaveInstanceStateItemIndex = -1;

        if (mContentType != ContentType.Applications) {
            setContentType(ContentType.Applications);
        }

        if (mCurrentPage != 0) {
            invalidatePageData(0);
        }
    }

    private AppsCustomizeTabHost getTabHost() {
        return (AppsCustomizeTabHost) mLauncher.findViewById(R.id.apps_customize_pane);
    }

    protected int getAssociatedLowerPageBound(int page) {
        final int count = getChildCount();
        int windowSize = Math.min(count, sLookBehindPageCount + sLookAheadPageCount + 1);
        return Math.max(Math.min(page - sLookBehindPageCount, count - windowSize), 0);
    }

    protected int getAssociatedUpperPageBound(int page) {
        final int count = getChildCount();
        int windowSize = Math.min(count, sLookBehindPageCount + sLookAheadPageCount + 1);
        return Math.min(Math.max(page + sLookAheadPageCount, windowSize - 1),
                count - 1);
    }

    protected String getCurrentPageDescription() {
        int page = (mNextPage != INVALID_PAGE) ? mNextPage : mCurrentPage;
        int stringId = R.string.default_scroll_format;
        int count;

        if (mContentType == ContentType.Applications) {
            //stringId = R.string.apps_customize_apps_scroll_format;
            count = mNumAppsPages;
        } else {
            throw new RuntimeException("Invalid ContentType");
        }

        return String.format(getContext().getString(stringId), page + 1, count);
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelDragging();
        super.onDetachedFromWindow();
    }

    /**
     * The different content types that this paged view can show.
     */
    public enum ContentType {
        Applications
    }

    /**
     * The different sort modes that can be used to order items
     */
    public enum SortMode {
        Title,
        LaunchCount,
        InstallTime
    }
}
