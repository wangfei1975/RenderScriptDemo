package com.wterry.fei.renderscriptdemo;

import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Script;
import android.support.v8.renderscript.Type;
import android.view.Surface;

/**
 * Created by feiwang on 15-10-09.
 */
public class RsNV12ToARGB {
    final ScriptC_NV12ToARGB mScript;
    final RenderScript mRS;
    final Allocation mIn;
    final Allocation mOffsetY;
    final int mWidth;
    final int mHeight;

    public RsNV12ToARGB(RenderScript rs, final int width, final int height) {

        mRS = rs;
        mScript = new ScriptC_NV12ToARGB(rs);

        mWidth = width;
        mHeight = height;

        // Type typeDst = new Type.Builder(mRS, Element.U8(mRS)).setX(width).setY(height * 3 / 2).create();
        // mIn = Allocation.createTyped(mRS, typeDst, Allocation.USAGE_SCRIPT);
        mIn = Allocation.createSized(mRS, Element.U8(mRS), width * height * 3/2, Allocation.USAGE_SCRIPT);
        mOffsetY = Allocation.createSized(mRS, Element.I32(mRS),  height * 3/2, Allocation.USAGE_SCRIPT);

        mScript.bind_src(mIn);
        mScript.bind_offset_y(mOffsetY);

        mScript.invoke_prepare(width, height);
    }

    public Bitmap convert(final byte [] nv12) {
        //   int w = ((mWidth + 3) & (~3));
        return convert(Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888), nv12);
    }

    public Bitmap convert1d(Bitmap bmp, Allocation in) {
        mScript.bind_src(in);
        Allocation out = Allocation.createFromBitmap(mRS, bmp);
        mScript.forEach_convert(out);
        return bmp;
    }
    public Bitmap convert(Bitmap bmp, Allocation in) {
        mScript.set_src_2d(in);
        Allocation out = Allocation.createFromBitmap(mRS, bmp);
        mScript.forEach_convert2d(out);
        out.copyTo(bmp);
        return bmp;
    }
    public Bitmap convert(Bitmap bmp, final byte []nv12) {
        mIn.copyFrom(nv12);
        Allocation out = Allocation.createFromBitmap(mRS, bmp);
        mScript.forEach_convert(out);
        out.copyTo(bmp);
        return bmp;
    }

    public void convert(final byte[]nv12, Surface sur) {
        mIn.copyFrom(nv12);
        Allocation out  = Allocation.createTyped(mRS,
                new Type.Builder(mRS, Element.RGBA_8888(mRS))
                        .setX(mWidth).setY(mHeight).create(),
                Allocation.USAGE_IO_OUTPUT | Allocation.USAGE_SCRIPT);
        out.setSurface(sur);
        mScript.forEach_convert2d(out);
        out.ioSend();
    }

    public void destroy() {
        mIn.destroy();
        mOffsetY.destroy();
        mScript.destroy();
    }
}
