package com.wterry.fei.renderscriptdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by feiwang on 15-03-09.
 */
public class Utils {

   static public byte[] loadRawResource(final Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);
        try {
            byte[] buf = new byte[inputStream.available()];
            inputStream.read(buf);
            return buf;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    static final int [] L1 = new int[256];
    static final int [] L2 = new int[256];
    static final int [] L3 = new int[256];
    static final int [] L4 = new int[256];
    static final int [] L5 = new int[256];
    static {
        for (int i = 0; i < 256; i++) {
            L1[i] = (int) (1.164 * (i - 16));
            L2[i] = (int) (1.596 * (i - 128));
            L3[i] = (int) (-0.813 * (i - 128));
            L4[i] = (int) (2.018 * (i - 128));
            L5[i] = (int) (-0.391 * (i - 128));
        }
    }
    static int YUVToARGB(final int Y, final int U, final int V) {
        int r = L1[Y] + L2[V];
        int g = L1[Y] + L3[U] + L5[V];
        int b = L1[Y] + L4[U];
        if (r < 0) r = 0;
        if (g < 0) g = 0;
        if (b < 0) b = 0;
        if (r > 255) r = 255;
        if (g > 255) g = 255;
        if (b > 255) b = 255;
        return Color.argb(255, r, g, b);
    }

    static int [] NV12ToARGB(final byte[] yuv, final int w, final int h) {
        int [] out = new int[w * h];
        int yOffset = 0;
        int uvOffset = w * h;
        for (int r = 0; r < h; r++) {
            int pos = 0;
            for (int c = 0; c < w; c++) {
                final int y = yuv[yOffset]&0xFF;
                final int u = yuv[uvOffset + pos]&0xFF;
                final int v = yuv[uvOffset + pos + 1]&0xFF;
                if (c%2 == 1) {
                    pos += 2;
                }
                out[yOffset++] = YUVToARGB(y, u, v);
            }
            if (r%2 == 1) {
                uvOffset += w;
            }
        }
        return out;
    }
    public static Bitmap NV12ToBitmap(final byte [] buf, final int w, final int h) {
        return Bitmap.createBitmap(NV12ToARGB(buf, w, h), w, h, Bitmap.Config.ARGB_8888);
    }

}
