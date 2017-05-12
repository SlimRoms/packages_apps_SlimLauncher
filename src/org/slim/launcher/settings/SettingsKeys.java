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

package org.slim.launcher.settings;

public interface SettingsKeys {

    String DEFAULT_HOMESCREEN = "default_homescreen";

    // Homescreen
    String KEY_SHOW_SEARCH_BAR = "show_search_bar";
    String KEY_HOMESCREEN_GRID = "homescreen_grid";
    String KEY_SHOW_SHADOW = "show_shadow";
    String KEY_FULLSCREEN = "show_fullscreen";

    // Drawer
    String KEY_DRAWER_SEARCH_ENABLED = "drawer_search_bar";

    // Dock
    String KEY_DOCK_ICONS = "dock_icon_count";
    String KEY_DOCK_HIDE_BACKGROUND = "dock_hide_background";
    String KEY_DOCK_PADDING = "dock_padding";

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
