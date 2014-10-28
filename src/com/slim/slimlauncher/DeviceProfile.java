package com.slim.slimlauncher;

import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.slim.slimlauncher.settings.SettingsProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class DeviceProfile {
    String name;
    float minWidthDps;
    float minHeightDps;
    public float numRows;
    public float numColumns;
    public float iconSize;
    private int iconSizeOriginal;
    float iconTextSize;
    public float numHotseatIcons;
    float hotseatIconSize;

    boolean isLandscape;
    boolean isTablet;
    boolean isLargeTablet;
    boolean transposeLayoutWithOrientation;

    int desiredWorkspaceLeftRightMarginPx;
    int edgeMarginPx;
    int folderEdgeMarginPx;
    Rect defaultWidgetPadding;

    int widthPx;
    int heightPx;
    int availableWidthPx;
    int availableHeightPx;
    int iconSizePx;
    int iconTextSizePx;
    int cellWidthPx;
    int cellHeightPx;
    int folderBackgroundOffset;
    int folderIconSizePx;
    int folderCellWidthPx;
    int folderCellHeightPx;
    int hotseatCellWidthPx;
    int hotseatCellHeightPx;
    int hotseatIconSizePx;
    int originalHotseatIconSizePx;
    int hotseatBarHeightPx;
    int hotseatBarHeightPxOriginal;
    int hotseatAllAppsRank;
    public int allAppsNumRows;
    public int allAppsNumCols;
    int searchBarSpaceWidthPx;
    int searchBarSpaceMaxWidthPx;
    int searchBarSpaceHeightPx;
    int searchBarHeightPx;
    int pageIndicatorHeightPx;

    View pageIndicator;

    boolean showSearchBar;

    DeviceProfile(String n, float w, float h, float r, float c,
                  float is, float its, float hs, float his) {
        // Ensure that we have an odd number of hotseat items (since we need to place all apps)
        if (!AppsCustomizePagedView.DISABLE_ALL_APPS && hs % 2 == 0) {
            throw new RuntimeException("All Device Profiles must have an odd number of hotseat spaces");
        }

        name = n;
        minWidthDps = w;
        minHeightDps = h;
        numRows = r;
        numColumns = c;
        iconSize = is;
        iconTextSize = its;
        numHotseatIcons = hs;
        hotseatIconSize = his;
    }

    DeviceProfile(Context context,
                  ArrayList<DeviceProfile> profiles,
                  float minWidth, float minHeight,
                  int wPx, int hPx,
                  int awPx, int ahPx,
                  Resources resources) {
        DisplayMetrics dm = resources.getDisplayMetrics();
        ArrayList<DeviceProfileQuery> points =
                new ArrayList<DeviceProfileQuery>();
        transposeLayoutWithOrientation =
                resources.getBoolean(R.bool.hotseat_transpose_layout_with_orientation);
        minWidthDps = minWidth;
        minHeightDps = minHeight;

        ComponentName cn = new ComponentName(context.getPackageName(),
                this.getClass().getName());
        defaultWidgetPadding = AppWidgetHostView.getDefaultPaddingForWidget(context, cn, null);
        folderEdgeMarginPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);
        edgeMarginPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);
        folderEdgeMarginPx = edgeMarginPx;
        desiredWorkspaceLeftRightMarginPx = 2 * edgeMarginPx;
        pageIndicatorHeightPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_page_indicator_height);

        // Interpolate the rows
        for (DeviceProfile p : profiles) {
            points.add(new DeviceProfileQuery(p.minWidthDps, p.minHeightDps, p.numRows));
        }
        numRows = Math.round(invDistWeightedInterpolate(minWidth, minHeight, points));
        // Interpolate the columns
        points.clear();
        for (DeviceProfile p : profiles) {
            points.add(new DeviceProfileQuery(p.minWidthDps, p.minHeightDps, p.numColumns));
        }
        numColumns = Math.round(invDistWeightedInterpolate(minWidth, minHeight, points));
        // Interpolate the icon size
        points.clear();
        for (DeviceProfile p : profiles) {
            points.add(new DeviceProfileQuery(p.minWidthDps, p.minHeightDps, p.iconSize));
        }
        iconSize = invDistWeightedInterpolate(minWidth, minHeight, points);
        iconSizeOriginal = DynamicGrid.pxFromDp(iconSize, dm);
        iconSizePx = iconSizeOriginal;

        // Interpolate the icon text size
        points.clear();
        for (DeviceProfile p : profiles) {
            points.add(new DeviceProfileQuery(p.minWidthDps, p.minHeightDps, p.iconTextSize));
        }
        iconTextSize = invDistWeightedInterpolate(minWidth, minHeight, points);
        iconTextSizePx = DynamicGrid.pxFromSp(iconTextSize, dm);

        // Interpolate the hotseat size
        points.clear();
        for (DeviceProfile p : profiles) {
            points.add(new DeviceProfileQuery(p.minWidthDps, p.minHeightDps, p.numHotseatIcons));
        }
        numHotseatIcons = Math.round(invDistWeightedInterpolate(minWidth, minHeight, points));
        // Interpolate the hotseat icon size
        points.clear();
        for (DeviceProfile p : profiles) {
            points.add(new DeviceProfileQuery(p.minWidthDps, p.minHeightDps, p.hotseatIconSize));
        }
        // Hotseat
        hotseatIconSize = invDistWeightedInterpolate(minWidth, minHeight, points);
        originalHotseatIconSizePx = DynamicGrid.pxFromDp(hotseatIconSize, dm);
        hotseatIconSizePx = originalHotseatIconSizePx;
        hotseatAllAppsRank = (int) (numColumns / 2);

        // Calculate other vars based on Configuration
        updateFromConfiguration(resources, wPx, hPx, awPx, ahPx);

        // Search Bar
        searchBarSpaceMaxWidthPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_search_bar_max_width);
        searchBarHeightPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_search_bar_height);
        searchBarSpaceWidthPx = Math.min(searchBarSpaceMaxWidthPx, widthPx);

        // Calculate the actual text height
        Paint textPaint = new Paint();
        textPaint.setTextSize(iconTextSizePx);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        cellWidthPx = iconSizePx;
        cellHeightPx = iconSizePx + (int) Math.ceil(fm.bottom - fm.top);

        // At this point, if the cells do not fit into the available height, then we need
        // to shrink the icon size
        /*
        Rect padding = getWorkspacePadding(isLandscape ?
                CellLayout.LANDSCAPE : CellLayout.PORTRAIT);
        int h = (int) (numRows * cellHeightPx) + padding.top + padding.bottom;
        if (h > availableHeightPx) {
            float delta = h - availableHeightPx;
            int deltaPx = (int) Math.ceil(delta / numRows);
            iconSizePx -= deltaPx;
            iconSize = DynamicGrid.dpiFromPx(iconSizePx, dm);
            cellWidthPx = iconSizePx;
            cellHeightPx = iconSizePx + (int) Math.ceil(fm.bottom - fm.top);
        }
        */

        // Hotseat
        hotseatCellWidthPx = iconSizePx;
        hotseatCellHeightPx = iconSizePx;

        // Folder
        folderCellWidthPx = cellWidthPx + 3 * edgeMarginPx;
        folderCellHeightPx = cellHeightPx + (int) ((3f/2f) * edgeMarginPx);
        folderBackgroundOffset = -edgeMarginPx;
        folderIconSizePx = iconSizePx + 2 * -folderBackgroundOffset;

        updateFromPreferences(context);
    }

    void updateFromConfiguration(Resources resources, int wPx, int hPx,
                                 int awPx, int ahPx) {
        isLandscape = (resources.getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE);
        isTablet = resources.getBoolean(R.bool.is_tablet);
        isLargeTablet = resources.getBoolean(R.bool.is_large_tablet);
        widthPx = wPx;
        heightPx = hPx;
        availableWidthPx = awPx;
        availableHeightPx = ahPx;
    }

    public void updateFromPreferences(Context context) {
        showSearchBar = SettingsProvider.getBoolean(context,
                SettingsProvider.KEY_SHOW_SEARCH_BAR, true);

        searchBarSpaceHeightPx = searchBarHeightPx + (showSearchBar ? 2 * edgeMarginPx : 0);

        int prefNumRows = SettingsProvider.getCellCountY(
                context, SettingsProvider.KEY_HOMESCREEN_GRID, 4);
        if (prefNumRows > 0) {
            numRows = prefNumRows;
        }

        int prefNumColumns = SettingsProvider.getCellCountX(
                context, SettingsProvider.KEY_HOMESCREEN_GRID, 4);
        if (prefNumColumns > 0) {
            numColumns = prefNumColumns;
        }

        int prefIconSize = SettingsProvider.getInt(context, SettingsProvider.KEY_ICON_SIZE, 0);
        if (prefIconSize > 0) {
            iconSizePx = (int) ((double) prefIconSize / 100.0 * iconSizeOriginal);
            hotseatIconSizePx = (int) ((double) prefIconSize / 100.0 * originalHotseatIconSizePx);
            folderIconSizePx = iconSizePx + 2 * -folderBackgroundOffset;
            Paint textPaint = new Paint();
            textPaint.setTextSize(iconTextSizePx);

            Paint.FontMetrics fm = textPaint.getFontMetrics();
            cellWidthPx = iconSizePx;
            cellHeightPx = iconSizePx + (int) Math.ceil(fm.bottom - fm.top);
            folderCellWidthPx = cellWidthPx + 3 * edgeMarginPx;
            folderCellHeightPx = cellHeightPx + (int) ((3f/2f) * edgeMarginPx);
        }

        int prefNumHotseatIcons = SettingsProvider.getInt(
                context, SettingsProvider.KEY_DOCK_ICONS, 5);
        if (prefNumHotseatIcons > 0) {
            numHotseatIcons = prefNumHotseatIcons;
            hotseatAllAppsRank = (int) (numHotseatIcons / 2);
        }

        int prefAllAppNumRows = SettingsProvider.getCellCountY(
                context, SettingsProvider.KEY_DRAWER_GRID, 0);
        if (prefAllAppNumRows > 0) {
            allAppsNumRows = prefAllAppNumRows;
        } else {
            if (isLandscape) {
                int pageIndicatorOffset =
                        context.getResources().getDimensionPixelSize(
                                R.dimen.apps_customize_page_indicator_offset);
                allAppsNumRows = (availableHeightPx - pageIndicatorOffset - 4 * edgeMarginPx) /
                        (iconSizePx + iconTextSizePx + 2 * edgeMarginPx);
            } else {
                allAppsNumRows = (int) numRows + 1;
            }
            SettingsProvider.putCellCountY(context,
                    SettingsProvider.KEY_DRAWER_GRID, allAppsNumRows);
        }

        int prefAllAppNumCols = SettingsProvider.getCellCountX(
                context, SettingsProvider.KEY_DRAWER_GRID, 0);
        if (prefAllAppNumCols > 0) {
            allAppsNumCols = prefAllAppNumCols;
        } else {
            Rect padding = getWorkspacePadding(isLandscape ?
                    CellLayout.LANDSCAPE : CellLayout.PORTRAIT);
            allAppsNumCols = (availableWidthPx - padding.left - padding.right - 2 * edgeMarginPx) /
                    (iconSizePx + 2 * edgeMarginPx);
            SettingsProvider.putCellCountX(context,
                    SettingsProvider.KEY_DRAWER_GRID, allAppsNumCols);
        }

        hotseatBarHeightPxOriginal = iconSizePx + 4 * edgeMarginPx;
        if (SettingsProvider.getBoolean(context,
                SettingsProvider.KEY_HIDE_DOCK, false)) {
            hotseatBarHeightPx = 0;
        } else {
            hotseatBarHeightPx = hotseatBarHeightPxOriginal;
        }
    }

    private float dist(PointF p0, PointF p1) {
        return (float) Math.sqrt((p1.x - p0.x)*(p1.x-p0.x) +
                (p1.y-p0.y)*(p1.y-p0.y));
    }

    private float weight(PointF a, PointF b,
                         float pow) {
        float d = dist(a, b);
        if (d == 0f) {
            return Float.POSITIVE_INFINITY;
        }
        return (float) (1f / Math.pow(d, pow));
    }

    private float invDistWeightedInterpolate(float width, float height,
                                             ArrayList<DeviceProfileQuery> points) {
        float sum = 0;
        float weights = 0;
        float pow = 5;
        float kNearestNeighbors = 3;
        final PointF xy = new PointF(width, height);

        ArrayList<DeviceProfileQuery> pointsByNearness = points;
        Collections.sort(pointsByNearness, new Comparator<DeviceProfileQuery>() {
            public int compare(DeviceProfileQuery a, DeviceProfileQuery b) {
                return (int) (dist(xy, a.dimens) - dist(xy, b.dimens));
            }
        });

        for (int i = 0; i < pointsByNearness.size(); ++i) {
            DeviceProfileQuery p = pointsByNearness.get(i);
            if (i < kNearestNeighbors) {
                float w = weight(xy, p.dimens, pow);
                if (w == Float.POSITIVE_INFINITY) {
                    return p.value;
                }
                weights += w;
            }
        }

        for (int i = 0; i < pointsByNearness.size(); ++i) {
            DeviceProfileQuery p = pointsByNearness.get(i);
            if (i < kNearestNeighbors) {
                float w = weight(xy, p.dimens, pow);
                sum += w * p.value / weights;
            }
        }

        return sum;
    }

    Rect getWorkspacePadding(int orientation) {
        Rect padding = new Rect();
        if (orientation == CellLayout.LANDSCAPE &&
                transposeLayoutWithOrientation) {
            // Pad the left and right of the workspace with search/hotseat bar sizes
            padding.set(searchBarSpaceHeightPx, edgeMarginPx,
                    hotseatBarHeightPx, edgeMarginPx);
        } else {
            if (isTablet()) {
                // Pad the left and right of the workspace to ensure consistent spacing
                // between all icons
                int width = (orientation == CellLayout.LANDSCAPE)
                        ? Math.max(widthPx, heightPx)
                        : Math.min(widthPx, heightPx);
                // XXX: If the icon size changes across orientations, we will have to take
                //      that into account here too.
                int gap = (int) ((width - 2 * edgeMarginPx -
                        (numColumns * cellWidthPx)) / (2 * (numColumns + 1)));
                padding.set(edgeMarginPx + gap,
                        searchBarSpaceHeightPx,
                        edgeMarginPx + gap,
                        hotseatBarHeightPx + pageIndicatorHeightPx);
            } else {
                // Pad the top and bottom of the workspace with search/hotseat bar sizes
                padding.set(desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.left,
                        searchBarSpaceHeightPx,
                        desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.right,
                        hotseatBarHeightPx + pageIndicatorHeightPx);
            }
        }
        return padding;
    }

    // The rect returned will be extended to below the system ui that covers the workspace
    Rect getHotseatRect() {
        if (isVerticalBarLayout()) {
            return new Rect(availableWidthPx - hotseatBarHeightPx, 0,
                    Integer.MAX_VALUE, availableHeightPx);
        } else {
            return new Rect(0, availableHeightPx - hotseatBarHeightPx,
                    availableWidthPx, Integer.MAX_VALUE);
        }
    }

    int calculateCellWidth(int width, int countX) {
        return width / countX;
    }
    int calculateCellHeight(int height, int countY) {
        return height / countY;
    }

    boolean isPhone() {
        return !isTablet && !isLargeTablet;
    }
    boolean isTablet() {
        return isTablet;
    }
    boolean isLargeTablet() {
        return isLargeTablet;
    }

    boolean isVerticalBarLayout() {
        return isLandscape && transposeLayoutWithOrientation;
    }

    public void layout(Launcher launcher) {

        updateFromPreferences(launcher);

        FrameLayout.LayoutParams lp;
        boolean hasVerticalBarLayout = isVerticalBarLayout();

        // Layout the search bar space
        View searchBar = launcher.getSearchBar();
        lp = (FrameLayout.LayoutParams) searchBar.getLayoutParams();
        if (hasVerticalBarLayout) {
            // Vertical search bar
            lp.gravity = Gravity.TOP | Gravity.LEFT;
            lp.width = searchBarSpaceHeightPx;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            searchBar.setPadding(
                    0, 2 * edgeMarginPx, 0,
                    2 * edgeMarginPx);
        } else {
            // Horizontal search bar
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            lp.width = searchBarSpaceWidthPx;
            lp.height = searchBarSpaceHeightPx;
            searchBar.setPadding(
                    2 * edgeMarginPx,
                    2 * edgeMarginPx,
                    2 * edgeMarginPx, 0);
        }
        searchBar.setLayoutParams(lp);

        // Layout the search bar
        View qsbBar = launcher.getQsbBar();
        qsbBar.setVisibility(showSearchBar ? View.VISIBLE : View.GONE);
        ViewGroup.LayoutParams vglp = qsbBar.getLayoutParams();
        vglp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        vglp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        qsbBar.setLayoutParams(vglp);

        // Layout the voice proxy
        View voiceButtonProxy = launcher.findViewById(R.id.voice_button_proxy);
        if (voiceButtonProxy != null) {
            if (!hasVerticalBarLayout) {
                lp = (FrameLayout.LayoutParams) voiceButtonProxy.getLayoutParams();
                lp.gravity = Gravity.TOP | Gravity.END;
                lp.width = (widthPx - searchBarSpaceWidthPx) / 2 +
                        2 * iconSizePx;
                lp.height = searchBarSpaceHeightPx;
            }
        }

        // Layout the workspace
        View workspace = launcher.findViewById(R.id.workspace);
        lp = (FrameLayout.LayoutParams) workspace.getLayoutParams();
        lp.gravity = Gravity.CENTER;
        Rect padding = getWorkspacePadding(isLandscape
                ? CellLayout.LANDSCAPE
                : CellLayout.PORTRAIT);
        workspace.setPadding(padding.left, padding.top,
                padding.right, padding.bottom);
        workspace.setLayoutParams(lp);

        // Layout the hotseat
        View hotseat = launcher.findViewById(R.id.hotseat);
        lp = (FrameLayout.LayoutParams) hotseat.getLayoutParams();
        if (isVerticalBarLayout()) {
            // Vertical hotseat
            lp.gravity = Gravity.RIGHT;
            lp.width = hotseatBarHeightPx;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            hotseat.setPadding(0, 2 * edgeMarginPx,
                    2 * edgeMarginPx, 2 * edgeMarginPx);
        } else if (isTablet()) {
            // Pad the hotseat with the grid gap calculated above
            int gridGap = (int) ((widthPx - 2 * edgeMarginPx -
                    (numColumns * cellWidthPx)) / (2 * (numColumns + 1)));
            int gridWidth = (int) ((numColumns * cellWidthPx) +
                    ((numColumns - 1) * gridGap));
            int hotseatGap = (int) Math.max(0,
                    (gridWidth - (numHotseatIcons * hotseatCellWidthPx))
                            / (numHotseatIcons - 1));
            lp.gravity = Gravity.BOTTOM;
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = hotseatBarHeightPx;
            hotseat.setPadding(2 * edgeMarginPx + gridGap + hotseatGap, 0,
                    2 * edgeMarginPx + gridGap + hotseatGap,
                    2 * edgeMarginPx);
        } else {
            // For phones, layout the hotseat without any bottom margin
            // to ensure that we have space for the folders
            lp.gravity = Gravity.BOTTOM;
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = hotseatBarHeightPx;
            hotseat.findViewById(R.id.layout).setPadding(2 * edgeMarginPx, 0,
                    2 * edgeMarginPx, 0);
        }
        if (SettingsProvider.getBoolean(launcher,
                SettingsProvider.KEY_HIDE_DOCK, false)) {
            lp.width = 0;
            lp.height = 0;
        }
        hotseat.setLayoutParams(lp);

        boolean hideHomescreenIndicator = SettingsProvider.getBoolean(launcher,
                SettingsProvider.KEY_HOMESCREEN_HIDE_INDICATOR, false);

        // Layout the page indicators
        pageIndicator = launcher.findViewById(R.id.page_indicator);
        if (pageIndicator != null) {
            if (hasVerticalBarLayout || hideHomescreenIndicator) {
                // Hide the page indicators when we have vertical search/hotseat
                pageIndicator.setVisibility(View.GONE);
            } else {
                pageIndicator.setVisibility(View.VISIBLE);
                // Put the page indicators above the hotseat
                lp = (FrameLayout.LayoutParams) pageIndicator.getLayoutParams();
                lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp.bottomMargin = Math.max(hotseatBarHeightPx, lp.bottomMargin);
                pageIndicator.setLayoutParams(lp);
            }
        }
    }

    public void updatePageIndicator(Context context) {
        if (pageIndicator != null) {
            boolean hideHomescreenIndicator = SettingsProvider.getBoolean(context,
                    SettingsProvider.KEY_HOMESCREEN_HIDE_INDICATOR, false);
            if (hideHomescreenIndicator) {
                pageIndicator.setVisibility(View.GONE);
            } else {
                pageIndicator.setVisibility(View.VISIBLE);
            }
            FrameLayout.LayoutParams lp =
                    (FrameLayout.LayoutParams) pageIndicator.getLayoutParams();
            lp.bottomMargin = getNavigationBarHeight(context) + hotseatBarHeightPx;
        }
    }

    private int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int orientation = context.getResources().getConfiguration().orientation;

        int id = resources.getIdentifier(
                orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape",
                "dimen", "android");
        if (id > 0) {
            return resources.getDimensionPixelSize(id);
        }
        return 0;
    }
}
