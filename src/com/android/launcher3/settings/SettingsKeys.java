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

package com.android.launcher3.settings;

public interface SettingsKeys {

    String DEFAULT_HOMESCREEN = "default_homescreen";
    String ALLOW_ROTATION = "allow_rotation";

    // Homescreen
    String KEY_SHOW_SEARCH_BAR = "show_search_bar";
    String KEY_HOMESCREEN_GRID = "homescreen_grid";
    String KEY_HOMESCREEN_ICON_SIZE = "homescreen_icon_size";

    // Drawer
    String KEY_PORTRAIT_DRAWER_GRID = "portrait_drawer_grid";
    String KEY_LANDSCAPE_DRAWER_GRID = "landscape_drawer_grid";
    String KEY_DRAWER_ICON_SIZE = "drawer_icon_size";

    // Dock
    String KEY_DOCK_ICONS = "dock_icon_count";
    String KEY_DOCK_ICON_SIZE = "dock_icon_size";
    String KEY_HIDDEN_APPS = "hidden_apps";
    String KEY_DRAWER_TYPE = "drawer_style";
    String KEY_DRAWER_SORT_MODE = "drawer_sort_mode";

    // Folder
    String FOLDER_BACKGROUND_COLOR = "folder_background_color";
    String FOLDER_ICON_TEXT_COLOR = "folder_icon_text_color";
    String FOLDER_PREVIEW_COLOR = "folder_preview_color";
    String HIDE_FOLDER_NAME = "hide_folder_name";

    // Gestures
    String LEFT_UP_GESTURE_ACTION = "left_up_gesture_action";
    String MIDDLE_UP_GESTURE_ACTION = "middle_up_gesture_action";
    String RIGHT_UP_GESTURE_ACTION = "right_up_gesture_action";
    String LEFT_DOWN_GESTURE_ACTION = "left_down_gesture_action";
    String MIDDLE_DOWN_GESTURE_ACTION = "middle_down_gesture_action";
    String RIGHT_DOWN_GESTURE_ACTION = "right_down_gesture_action";
    String PINCH_GESTURE_ACTION = "pinch_gesture_action";
    String SPREAD_GESTURE_ACTION = "spread_gesture_action";
    String DOUBLE_TAP_GESTURE_ACTION = "double_tap_gesture_action";

}
