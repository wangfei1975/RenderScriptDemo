package com.wterry.fei.renderscriptdemo;

import android.graphics.Bitmap;
import android.support.v8.renderscript.RenderScript;

/**
 * Created by feiwang on 15-03-13.
 */
public final class NV12Image {
    final int mWidth;
    final int mHeight;
    final byte[] mData;

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    byte [] getData() {
        return mData;
    }
    public NV12Image(int w, int h, byte[] data) {
        if (data == null || data.length < w * h * 3 / 2) {
            throw new IllegalArgumentException("invalid data");
        }
        mWidth = w;
        mHeight = h;
        mData = data;
    }

    public NV12Image(int w, int h) {
        mWidth = w;
        mHeight = h;
        mData = new byte[w * h * 3 / 2];
    }

    public NV12Image(final RenderScript rs, final Bitmap bmp) {
        mWidth = bmp.getWidth();
        mHeight = bmp.getHeight();
        mData = new RsARGBToNV12(rs, mWidth, mHeight).convert(bmp);
    }

    public void fromBitmap(final RenderScript rs, final Bitmap bmp) {
         new RsARGBToNV12(rs, mWidth, mHeight).convert(mData, bmp);
    }
    public Bitmap toBitmap(final RenderScript rs) {
        return toBitmap(rs, Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888));
    }

    public Bitmap toBitmap(final RenderScript rs, final Bitmap bmp) {
       return  new RsNV12ToARGB(rs, mWidth, mHeight).convert(bmp, mData);
    }

    public NV12Image blit(final RenderScript rs, final Bitmap bmp, int left, int top) {
        return new RsARGBToNV12(rs, mWidth, mHeight, left, top).blit(this, bmp);
    }
}
