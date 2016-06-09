package com.wterry.fei.renderscriptdemo;

import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;

/**
 * Created by feiwang on 15-03-11.
 */
public class RsARGBToNV12 {
    final ScriptC_ARGBToNV12 mScript;
    final RenderScript mRS;
    final Allocation mOut;
    final Allocation mOffsetY;
    final int mWidth;
    final int mHeight;
    final int mStartX;
    final int mStartY;
    public RsARGBToNV12(RenderScript rs, final int width, final int height) {
        this(rs, width, height, 0, 0);
    }
    public RsARGBToNV12(RenderScript rs, final int width, final int height, final int startX, final int startY) {
        mRS = rs;
        mScript = new ScriptC_ARGBToNV12(rs);

        mWidth = width;
        mHeight = height;
        mStartX = startX;
        mStartY = startY;

        mOut = Allocation.createSized(mRS, Element.U8(mRS), width * height * 3/2, Allocation.USAGE_SCRIPT);
        mOffsetY = Allocation.createSized(mRS, Element.I32(mRS),  height * 3/2, Allocation.USAGE_SCRIPT);
        mScript.bind_out(mOut);
        mScript.bind_offset_y(mOffsetY);
        mScript.invoke_prepare_converter(width, height, startX, startY);
    }

    public byte [] convert(final Bitmap bmp) {
        return convert(new byte [mWidth * mHeight * 3/2], bmp);
    }

    public byte [] convert(byte [] nv12, final Bitmap bmp) {
        mScript.forEach_convert(Allocation.createFromBitmap(mRS, bmp));
        mOut.copyTo(nv12);
        return nv12;
    }

    public NV12Image blit(NV12Image dst, final Bitmap bmp) {
        mOut.copyFrom(dst.getData());
        mScript.forEach_blit(Allocation.createFromBitmap(mRS, bmp));
        mOut.copyTo(dst.getData());
        return dst;
    }

    public void destroy() {
        mOut.destroy();
        mOffsetY.destroy();
        mScript.destroy();
    }
}
