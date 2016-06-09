package com.wterry.fei.renderscriptdemo;

import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Script;
import android.support.v8.renderscript.Type;

/**
 * Created by feiwang on 15-03-13.
 */
public final class RsNV12ImageJointer {

    public static final int ROTATION_0 = 0;
    public static final int ROTATION_90 = 90;
    public static final int ROTATION_180 = 180;
    public static final int ROTATION_270 = 270;

    public static final int MIRROR_NONE = 0;
    public static final int MIRROR_HORIZONTAL = 1;
    public static final int MIRROR_VERTICAL = 2;


    final ScriptC_NV12ImageJoint mScript;
    final RenderScript mRS;
    final Allocation mOut;
    final Allocation mIn;
    final Script.LaunchOptions mOption;
    final Script.LaunchOptions mUVOption;
    final int mRotation;
    final int mMirror;
    final int mSrcWidth;
    final int mSrcHeight;
    final int mDstWidth;
    final int mDstHeight;
    final int mDstStrideX;
    final int mDstStrideY;
    final int mDstX;
    final int mDstY;


    static private void checkParams(final int sw, final int sh,
                                    final int cl, final int ct, final int cr, final int cb,
                                    final int sdx, final int sdy,
                                    final int dx, final int dy,
                                    final int dw, final int dh,
                                    final int rot, final int mir) {
        if (sw <= 0 || sh <= 0) {
            throw new IllegalArgumentException("source width and height can not be zero:" + sw + "x" + sh);
        }
        if (cl < 0 || cl >= sw-1) {
            throw new IllegalArgumentException("illegal crop left " + cl);
        }
        if (cr <= 0 || cr > sw-1) {
            throw new IllegalArgumentException("illegal crop right " + cr);
        }
        if (cl >= cr) {
            throw new IllegalArgumentException("illegal crop left and right  left:" + cl + " right:" + cr);
        }
        if (ct < 0 || ct >= sh - 1) {
            throw new IllegalArgumentException("illegal crop top " + ct);
        }
        if (cb < 0 || cb > sh - 1) {
            throw new IllegalArgumentException("illegal crop bottom " + cb);
        }
        if (ct >= cb) {
            throw new IllegalArgumentException("illegal crop top and bottom top:" + ct + " bottom:" + cb);
        }

        if (dw <= 0 || dh <= 0) {
            throw new IllegalArgumentException("dest width and height can not be zero:" + dw + "x" + dh);
        }

        //TODO check sdx, sdy, dx, dy.
        if (rot != ROTATION_0 && rot != ROTATION_90 && rot != ROTATION_180 && rot != ROTATION_270) {
            throw new IllegalArgumentException("illegal rotation:" + rot);
        }
        if (mir != MIRROR_NONE && mir != MIRROR_VERTICAL && mir != MIRROR_HORIZONTAL) {
            throw new IllegalArgumentException("illegal mirror:" + rot);
        }
    }

    public static final class Builder {
        private final RenderScript rs;
        private int sw, sh, dw, dh, cl, ct, cr,cb, rot, mir;
        private int dstx, dsty, stridex, stridey;
        public Builder(RenderScript r) {
            rs = r;
        }
        public Builder setSrcSize(int w, int h) {
            sw = w;
            sh = h;
            return this;
        }
        public Builder setDstSize(int w, int h) {
            dw = w;
            dh = h;
            return this;
        }
        public Builder setCrop(int cropLeft, int cropTop, int cropRight, int cropBottom) {
            cl = cropLeft;
            ct = cropTop;
            cr = cropRight;
            cb = cropBottom;
            return this;
        }
        public Builder setRotation(int rotation) {
            rot = rotation;
            return this;
        }
        public Builder setMirror(int mirror) {
            mir = mirror;
            return this;
        }

        public Builder setStride(int sdx, int sdy) {
            stridex = sdx;
            stridey = sdy;
            return this;
        }
        public Builder setDstOffset(int xoff, int yoff) {
            dstx = xoff;
            dsty = yoff;
            return this;
        }

         public RsNV12ImageJointer build() {
            checkParams(sw, sh,cl, ct, cr, cb, stridex, stridey,  dstx, dsty, dw, dh, rot, mir);
            return new RsNV12ImageJointer(rs, sw, sh, cl, ct, cr, cb, stridex, stridey, dstx, dsty, dw, dh, rot, mir);
        }
    }


   /*
    *   Create a processor that processes NV12 images.
    *   The result equal to do following process orderly.
    *     1. crop source by using cropLeft, cropTop, cropRight, cropBottom
    *     2. Resize the corpped image to dstWidth, dstHeight
    *     3. rotation the resized image by rotation
    *     4. mirror the result
    *
    *    The cropLeft, cropTop, cropRight, cropBottom specify a rectangle area
    *      must inside srcWidth and srcHeight
    *
    *    Rotation can be one of 0, 90, 180, 270
    *    Mirror can be one of MIRROR_NONE, MIRROR_HORIZONTAL, MIRROR_VERTICAL
    *
    * */
    private RsNV12ImageJointer(final RenderScript rs,
                               final int srcWidth,
                               final int srcHeight,
                               final int cropLeft,
                               final int cropTop,
                               final int cropRight,
                               final int cropBottom,
                               final int dstStrideX,
                               final int dstStrideY,
                               final int dstX,
                               final int dstY,
                               final int dstWidth,
                               final int dstHeight,
                               final int rotation,
                               final int mirror) {

        mRS = rs;
        mScript = new ScriptC_NV12ImageJoint(mRS);
        mRotation = rotation;
        mMirror = mirror;
        mSrcWidth = srcWidth;
        mSrcHeight = srcHeight;
        mIn = Allocation.createSized(mRS, Element.U8(mRS), srcWidth * srcHeight * 3 / 2, Allocation.USAGE_SCRIPT);

        Allocation mapX = Allocation.createSized(mRS, Element.U16(mRS), dstWidth, Allocation.USAGE_SCRIPT);
        Allocation mapY = Allocation.createSized(mRS, Element.U16(mRS), dstHeight, Allocation.USAGE_SCRIPT);
        Allocation offY = Allocation.createSized(mRS, Element.U32(mRS), srcHeight * 3 / 2, Allocation.USAGE_SCRIPT);

        mScript.bind_map_x(mapX);
        mScript.bind_map_y(mapY);
        mScript.bind_offset_y(offY);

        mScript.bind_src(mIn);

        mScript.invoke_prepare_jointer(srcWidth, srcHeight, cropLeft, cropTop, cropRight, cropBottom,
                dstStrideX,
                dstStrideY,
                dstX, dstY,
                dstWidth, dstHeight);

        if (rotation == ROTATION_90 || rotation == ROTATION_270) {
            mDstWidth = dstHeight;
            mDstHeight = dstWidth;
        } else {
            mDstWidth = dstWidth;
            mDstHeight = dstHeight;

        }

        mDstStrideX = dstStrideX;
        mDstStrideY = dstStrideY;
        mDstX = dstX;
        mDstY = dstY;
        Type typeDst = new Type.Builder(mRS, Element.U8(mRS)).setX(mDstStrideX).setY(mDstStrideY * 3 / 2).create();
        mOut = Allocation.createTyped(mRS, typeDst, Allocation.USAGE_SCRIPT);
        mOption = new Script.LaunchOptions().setX(mDstX, mDstX+mDstWidth).setY(mDstY, mDstY+mDstHeight);
        mUVOption = new Script.LaunchOptions().setX(mDstX, mDstX+mDstWidth).setY(mDstStrideY+mDstY/2, mDstStrideY + (mDstY + mDstHeight)/2);
    }

    private void process() {
        switch(mRotation + mMirror) {
            case 0:
            default:
                mScript.forEach_crop_resize_rot00_mir0_y(mOut, mOption);
                mScript.forEach_crop_resize_rot00_mir0_uv(mOut, mUVOption);
                break;
            case 1:
            case 182:  // rot 180 then mirror vertical same as mirror horizontal
                mScript.forEach_crop_resize_rot00_mir1_y(mOut, mOption);
                mScript.forEach_crop_resize_rot00_mir1_uv(mOut, mUVOption);
                break;
            case 2:
            case 181:   // rot 180 then mirror horizontal same as mirror vertical
                mScript.forEach_crop_resize_rot00_mir2_y(mOut, mOption);
                mScript.forEach_crop_resize_rot00_mir2_uv(mOut, mUVOption);
                break;
            case 90:
                mScript.forEach_crop_resize_rot90_mir0_y(mOut, mOption);
                mScript.forEach_crop_resize_rot90_mir0_uv(mOut, mUVOption);
                break;
            case 91:
            case 272: // rot 270 ten mirror vertical same as rot 90 then mirror horizontal
                mScript.forEach_crop_resize_rot90_mir1_y(mOut, mOption);
                mScript.forEach_crop_resize_rot90_mir1_uv(mOut, mUVOption);
                break;
            case 92:
            case 271: // rot 270 ten mirror horizontal same as rot 90 then mirror vertical
                mScript.forEach_crop_resize_rot90_mir2_y(mOut, mOption);
                mScript.forEach_crop_resize_rot90_mir2_uv(mOut, mUVOption);
                break;
            case 180:
                mScript.forEach_crop_resize_rot180_mir0_y(mOut, mOption);
                mScript.forEach_crop_resize_rot180_mir0_uv(mOut, mUVOption);
                break;
            case 270:
                mScript.forEach_crop_resize_rot270_mir0_y(mOut, mOption);
                mScript.forEach_crop_resize_rot270_mir0_uv(mOut, mUVOption);
                break;
        }

    }


    NV12Image process(final NV12Image in) {
       return process(new NV12Image(mDstStrideX, mDstStrideY), in);
    }

    void setBackground(final NV12Image bk) {
        mOut.copyFrom(bk.getData());
    }
    /*
    *  caller should make sure in and out size match the parameters when build the processor
    * */
    NV12Image process(NV12Image out, final NV12Image in) {
        mIn.copyFrom(in.getData());
        process();
        mOut.copyTo(out.getData());
        return out;
    }
    Allocation pipeProcess(final NV12Image in) {
        mIn.copyFrom(in.getData());
        process();
        return mOut;
    }
    public void destroy() {
        mIn.destroy();
        mOut.destroy();
        mScript.destroy();
    }
}
