package org.slim.launcher.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.android.launcher3.LauncherAppState;

import org.slim.launcher.IconPackHelper;

/**
 * Created by gmillz on 3/24/17.
 */

public class SlimUtils {

    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();

    private static int getIconBitmapSize() {
        return LauncherAppState.getInstance().getInvariantDeviceProfile().iconBitmapSize;
    }

    public static Drawable getDrawableForCustomIcon(Context context, String customIconResource) {
        String[] splitResource = customIconResource.split("\\|");
        if (splitResource.length != 2) return null;
        String packageName = splitResource[0];
        String resource = splitResource[1];
        PackageManager pm = context.getPackageManager();
        Resources resources;
        try {
            resources = pm.getResourcesForApplication(packageName);
            int id = resources.getIdentifier(resource, "drawable", packageName);
            return resources.getDrawableForDensity(id,
                    LauncherAppState.getInstance().getInvariantDeviceProfile().fillResIconDpi,
                    context.getTheme());
        } catch (PackageManager.NameNotFoundException|Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a bitmap suitable for the all apps view.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context,
                                          IconPackHelper iconPackHelper) {
        synchronized (sCanvas) {
            final int iconBitmapSize = getIconBitmapSize();

            Drawable iconMask = null;
            Drawable iconBack = null;
            Drawable iconPaletteBack = null;
            Drawable iconUpon = null;
            float scale = 1f;
            float angle = 0;
            float translationX = 0;
            float translationY = 0;
            int defaultSwatchColor = 0;
            int backTintColor = 0;
            IconPackHelper.SwatchType swatchType = IconPackHelper.SwatchType.None;
            float[] colorFilter = null;

            if (iconPackHelper != null) {
                iconMask = iconPackHelper.getIconMask();
                iconBack = iconPackHelper.getIconBack();
                iconPaletteBack = iconPackHelper.getIconPaletteBack();
                iconUpon = iconPackHelper.getIconUpon();
                scale = iconPackHelper.getIconScale();
                angle = iconPackHelper.getIconAngle();
                translationX = iconPackHelper.getTranslationX();
                translationY = iconPackHelper.getTranslationY();
                swatchType = iconPackHelper.getSwatchType();
                colorFilter = iconPackHelper.getColorFilter();
            }

            int width = iconBitmapSize;
            int height = iconBitmapSize;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = iconBitmapSize;
            int textureHeight = iconBitmapSize;

            Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            if (swatchType != null && swatchType != IconPackHelper.SwatchType.None) {
                Palette.Builder builder = new Palette.Builder(bitmap);
                builder.maximumColorCount(IconPackHelper.NUM_PALETTE_COLORS);
                Palette palette = builder.generate();
                switch (swatchType) {
                    case Vibrant:
                        backTintColor = palette.getVibrantColor(defaultSwatchColor);
                        break;
                    case VibrantLight:
                        backTintColor = palette.getLightVibrantColor(defaultSwatchColor);
                        break;
                    case VibrantDark:
                        backTintColor = palette.getDarkVibrantColor(defaultSwatchColor);
                        break;
                    case Muted:
                        backTintColor = palette.getMutedColor(defaultSwatchColor);
                        break;
                    case MutedLight:
                        backTintColor = palette.getLightMutedColor(defaultSwatchColor);
                        break;
                    case MutedDark:
                        backTintColor = palette.getDarkMutedColor(defaultSwatchColor);
                        break;
                }
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left + width, top + height);
            canvas.save();
            final float halfWidth = width / 2f;
            final float halfHeight = width / 2f;
            canvas.rotate(angle, halfWidth, halfHeight);
            canvas.scale(scale, scale, halfWidth, halfHeight);
            canvas.translate(translationX, translationY);
            if (colorFilter != null) {
                Paint p = null;
                if (icon instanceof BitmapDrawable) {
                    p = ((BitmapDrawable) icon).getPaint();
                } else if (icon instanceof PaintDrawable) {
                    p = ((PaintDrawable) icon).getPaint();
                }
                if (p != null) {
                    p.setColorFilter(new ColorMatrixColorFilter(colorFilter));
                }
            }
            icon.draw(canvas);
            canvas.restore();
            if (iconMask != null) {
                iconMask.setBounds(icon.getBounds());
                ((BitmapDrawable) iconMask).getPaint().setXfermode(
                        new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                iconMask.draw(canvas);
            }
            Drawable back = null;
            if (swatchType != null && swatchType != IconPackHelper.SwatchType.None) {
                back = iconPaletteBack;
                defaultSwatchColor = iconPackHelper.getDefaultSwatchColor();
            } else if (iconBack != null) {
                back = iconBack;
            }
            if (back != null) {
                canvas.setBitmap(null);
                Bitmap finalBitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                        Bitmap.Config.ARGB_8888);
                canvas.setBitmap(finalBitmap);
                back.setBounds(icon.getBounds());
                Paint paint = ((BitmapDrawable) back).getPaint();
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                if (backTintColor != 0) {
                    paint.setColorFilter(new PorterDuffColorFilter(backTintColor,
                            PorterDuff.Mode.MULTIPLY));
                }
                back.draw(canvas);
                canvas.drawBitmap(bitmap, null, icon.getBounds(), null);
                bitmap = finalBitmap;
            }
            if (iconUpon != null) {
                iconUpon.draw(canvas);
            }
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }
}
