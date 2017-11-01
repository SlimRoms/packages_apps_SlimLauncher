package org.slim.launcher;

import android.graphics.Color;

public class ColorUtil {

    public static boolean isDarkColor(int color) {
        double darkness = 1-(0.299 * Color.red(color)
                + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;

        return darkness > 0.5;
    }
}
