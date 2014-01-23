/*
 * Copyright (C) 2015 The SlimRoms Project
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

package com.slim.slimlauncher.settings;

public interface SettingsKeys {

    public static final String DEFAULT_HOMESCREEN = "default_homescreen";

    // Homescreen
    public static final String KEY_SHOW_SEARCH_BAR = "show_search_bar";
    public static final String KEY_HOMESCREEN_GRID = "homescreen_grid";
    public static final String KEY_HOMESCREEN_HIDE_LABELS = "homescreen_hide_icon_labels";
    public static final String KEY_HOMESCREEN_ICON_SIZE = "homescreen_icon_size";

    // General
    public static final String KEY_NOTIFICATION_BADGES = "notification_badges";
    public static final String KEY_LOCK_WORKSPACE = "lock_workspace";
    public static final String KEY_ICON_PACK = "icon_pack";

    // Drawer
    public static final String KEY_DRAWER_STYLE = "drawer_style";
    public static final String KEY_DRAWER_GRID = "drawer_grid";
    public static final String KEY_DRAWER_HIDE_LABELS = "drawer_hide_icon_labels";
    public static final String KEY_DRAWER_SORT_MODE = "drawer_sort_mode";
    public static final String KEY_DRAWER_ICON_SIZE = "drawer_icon_size";

    // Dock
    public static final String KEY_DOCK_ICONS = "dock_icon_count";
    public static final String KEY_DOCK_HIDE_LABELS = "dock_hide_icon_labels";
    public static final String KEY_DOCK_ICON_SIZE = "dock_icon_size";

    // Folder
    public static final String FOLDER_BACKGROUND_COLOR = "folder_background_color";
    public static final String FOLDER_ICON_TEXT_COLOR = "folder_icon_text_color";
    public static final String FOLDER_PREVIEW_COLOR = "folder_preview_color";
    public static final String HIDE_FOLDER_NAME = "hide_folder_name";
    public static final String KEY_SMART_FOLDER = "smart_folder";
    public static final String KEY_SCROLLING_FOLDERS = "scrolling_folders";

    public static final String KEY_HIDDEN_APPS = "hidden_apps";

    // button actions
    public static final String KEY_HOME_BUTTON_DEFAULT_SCREEN = "home_button_default_screen";
    public static final String KEY_HOME_BUTTON_ACTION = "home_button_action";
    public static final String KEY_MENU_BUTTON_ACTION = "menu_button_action";
    public static final String KEY_BACK_BUTTON_ACTION = "back_button_action";

    // Gestures
    public static final String LEFT_UP_GESTURE_ACTION = "left_up_gesture_action";
    public static final String MIDDLE_UP_GESTURE_ACTION = "middle_up_gesture_action";
    public static final String RIGHT_UP_GESTURE_ACTION = "right_up_gesture_action";
    public static final String LEFT_DOWN_GESTURE_ACTION = "left_down_gesture_action";
    public static final String MIDDLE_DOWN_GESTURE_ACTION = "middle_down_gesture_action";
    public static final String RIGHT_DOWN_GESTURE_ACTION = "right_down_gesture_action";
    public static final String PINCH_GESTURE_ACTION = "pinch_gesture_action";
    public static final String SPREAD_GESTURE_ACTION = "spread_gesture_action";
    public static final String DOUBLE_TAP_GESTURE_ACTION = "double_tap_gesture_action";

}
