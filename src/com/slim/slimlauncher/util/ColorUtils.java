/*
* Copyright (C) 2015 SlimRoms Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.slim.slimlauncher.util;

import android.graphics.Color;

public class ColorUtils {

    /**
     * Returns darker version of specified <code>color</code>.
     */
    public static int darker (int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red( color );
        int g = Color.green( color );
        int b = Color.blue( color );

        return Color.argb( a,
                Math.max( (int)(r * factor), 0 ),
                Math.max( (int)(g * factor), 0 ),
                Math.max( (int)(b * factor), 0 ) );
    }

    /*
     * Determines whether light or dark text color should be used based on background
     */
    public static boolean darkTextColor(int background) {
        // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - ( 0.299 * Color.red(background) + 0.587 * Color.green(background)
                + 0.114 * Color.blue(background)) / 255;
        return a < 0.5;
    }
}
