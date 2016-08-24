package org.slim.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.android.launcher3.R;


public class ShortcutHelper {

    public static final String ACTION_SLIM_LAUNCHER_SHORTCUT = "action_slim_launcher_shortcut";
    public static final String SHORTCUT_VALUE = "shortcut_value";

    public static final String SHORTCUT_ALL_APPS = "**open_app_drawer**";
    public static final String SHORTCUT_OVERVIEW = "**overview_mode**";
    public static final String SHORTCUT_SETTINGS = "**launcher_settings**";
    public static final String SHORTCUT_DEFAULT_PAGE = "**default_homescreen**";

    public static Bitmap getIcon(Context context, String value) {
        switch (value) {
            case SHORTCUT_ALL_APPS:
                return drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.ic_allapps));
            case SHORTCUT_OVERVIEW:
                return drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.ic_widget));
            case SHORTCUT_SETTINGS:
                return drawableToBitmap(ContextCompat.getDrawable(context,
                        R.mipmap.ic_launcher_settings));
            default:
                return drawableToBitmap(ContextCompat.getDrawable(
                        context, R.mipmap.ic_launcher_home));
        }
    }

    public static Bitmap drawableToBitmap(Drawable d) {
        if (d == null)
            return null;

        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(),
                d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);

        return bitmap;
    }
}
