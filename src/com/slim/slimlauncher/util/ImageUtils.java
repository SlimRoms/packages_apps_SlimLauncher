package com.slim.slimlauncher.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;

public class ImageUtils {

    public static Drawable getDrawableFromIntent(Context context, Intent intent) {
        final Resources res = context.getResources();
        final PackageManager pm = context.getPackageManager();
        ActivityInfo info = intent.resolveActivityInfo(pm, PackageManager.GET_ACTIVITIES);

        if (info == null) {
            return res.getDrawable(android.R.drawable.sym_def_app_icon);
        }

        Drawable icon = info.loadIcon(pm);
        return new BitmapDrawable(res, resizeImage(context, icon));
    }

    public static Bitmap resizeImage(Context context, Drawable icon) {
        Resources res = context.getResources();
        int size = (int) res.getDimension(android.R.dimen.app_icon_size);

        int width = size;
        int height = size;

        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        } else if (icon instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) icon;
            Bitmap bitmap = bd.getBitmap();
            if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                bd.setTargetDensity(res.getDisplayMetrics());
            }
        }

        int sourceWidth = icon.getIntrinsicWidth();
        int sourceHeight = icon.getIntrinsicHeight();

        if (sourceWidth > 0 && sourceHeight > 0) {
            if (width < sourceWidth || height < sourceHeight) {
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            } else if (sourceWidth < width && sourceHeight < height) {
                width = sourceWidth;
                height = sourceHeight;
            }
        }

        final Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);

        final int left = (size - width) / 2;
        final int top = (size - height) / 2;

        Rect oldBounds = new Rect();
        oldBounds.set(icon.getBounds());
        icon.setBounds(left, top, left + width, top + height);
        icon.draw(canvas);
        icon.setBounds(oldBounds);
        canvas.setBitmap(null);

        return bitmap;
    }
}
