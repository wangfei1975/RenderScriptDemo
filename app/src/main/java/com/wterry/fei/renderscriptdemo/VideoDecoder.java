package com.wterry.fei.renderscriptdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by feiwang on 15-04-07.
 */
public class VideoDecoder {

    static final String TAG = VideoDecoder.class.getSimpleName();
    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;
    private MediaFormat mInputFormat;
    private MediaFormat mOutputFormat;
    private long mDuration;

    ByteBuffer[] mInputBuffers;
    ByteBuffer[] mOutputBuffers;

    private Surface mSurface;
    private boolean mEos;


    private int mBufferWidth, mBufferHeight;
    private int mBufferSize;
    private int mVideoWidth, mVideoHeight;
    private Rect mBufferCrop = new Rect();
    private int  mColorFormat;

    private AtomicInteger mState = new AtomicInteger(0);


    Rect mSurRect;
    Rect mBmpRect;
    Rect mRenderRect;
    RenderScript mRS;
    NV12Image mNV12Image;
    RsNV12ToARGB mConverter;
    Bitmap mBitmap ;
    RsNV12ImageProcessor mProcessor;

    GLVideoRender mNV12View;

    VideoDecoder(RenderScript r, GLVideoRender texture, int surfaceWidth, int surfaceHeight) {
        if (texture != null) {
            mNV12View = texture;
            mSurface = new Surface(texture.getSurfaceTexture());
            mSurRect = new Rect(0, 0, surfaceWidth, surfaceHeight);
            Log.i(TAG, "Surface size: " + mSurRect.toString());
        }
        mRS = r;
    }

    Rect getRenderRect() {
        return mRenderRect;
    }



    RsNV12ImageProcessor buildProcessor(final RenderScript rs, final int dstWidth, final int dstHeight, final int rot) {

       int cl = 0, ct = 0, cr = 0, cb = 0;
      if (mUserCrop != null) {
          cl = (int)(mVideoWidth * mUserCrop.left);
          ct = (int)(mVideoHeight * mUserCrop.top);
          cr = (int)(mVideoWidth * mUserCrop.right);
          cb = (int)(mVideoHeight * mUserCrop.bottom);
      }
       return new RsNV12ImageProcessor.Builder(mRS).setSrcSize(mBufferWidth, mBufferHeight)
                .setCrop(mBufferCrop.left + cl, mBufferCrop.top + ct, mBufferCrop.right - cr, mBufferCrop.bottom - cb)
                .setRotation(rot)
                .setMirror(mMirror)
                .setDstSize(dstWidth, dstHeight).build();
    }
    long getDuration() {
        return mDuration;
    }
    Bitmap getThumbnail(Bitmap bmp, long pos) {
        if (mState.get() <= 0) {
            throw new IllegalStateException("Decoder does not initialized");
        }

        if (pos >= mDuration) {
            pos = mDuration - 50000;
        }
        mExtractor.seekTo(pos, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

        mDecoder.flush();

        int idx = -1;
        ByteBuffer buffer = null;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        while(idx < 0 || buffer == null || mState.get() < 3) {
            idx = dequeueOutputBuffer(0, info);
            if (idx >= 0 && info.presentationTimeUs < pos) {
                mDecoder.releaseOutputBuffer(idx, false);
                idx = -1;
                continue;
            }
            buffer = getOutputBuffer(idx);
        }
        buffer.get(mNV12Image.mData, 0, mNV12Image.mData.length);
        buffer.rewind();
        mDecoder.releaseOutputBuffer(idx, false);
        int w = Utils.align16(bmp.getWidth());
        int h = Utils.align16(bmp.getHeight());
        RsNV12ImageProcessor p = buildProcessor(mRS, w, h, mRotation);

        if (mRotation == 90 || mRotation == 270) {
            int t = w; w = h; h = t;
        }
        RsNV12ToARGB c = new RsNV12ToARGB(mRS, w, h);
        c.convert(bmp, p.pipeProcess(mNV12Image));
        p.destroy();
        c.destroy();
        return bmp;
    }

    boolean mRenderThread;

  //  final LinkedBlockingQueue<Integer> mBufQueue = new LinkedBlockingQueue<Integer>();

    Bitmap mWaterMark;

    public void setWaterMark(Bitmap waterMark) {
        mWaterMark = waterMark;
    }

    GLVideoRender.RenderListener mRenderListener = new GLVideoRender.RenderListener() {
        @Override
        public void onRenderComplete(int idx) {
            mDecoder.releaseOutputBuffer(idx, false);
        }
    };
//    byte [] tmp = new byte[1280*720*3/2];
    public void releaseOutputBuffer(final int index, boolean render) {

        if (mSurface == null || !render) {
            try {
                mDecoder.releaseOutputBuffer(index, false);
            } catch (IllegalStateException e) {

            }
            return;
        }
        mNV12View.renderBuffer(index, getOutputBuffer(index), mRenderListener);
     //   mNV12View.setData(this, index);
/*
        ByteBuffer b = getOutputBuffer(index);
        b.get(mNV12Image.mData, 0, mNV12Image.mData.length);



        mNV12View.requestRender();
        b.rewind();
        mDecoder.releaseOutputBuffer(index, false);
        */

//        mNV12View.bringToFront();


      /*
        try {
            mBufQueue.put(index);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (mRenderThread) {
            return;
        }

        mRenderThread = true;
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (mState.get() > 0) {
                    try {
                        int idx = mBufQueue.take();
                        if (mState.get() <= 0) {
                            break;
                        }
                        ByteBuffer b = getOutputBuffer(idx);
                        if (b != null) {
                            b.get(mNV12Image.mData, 0, mNV12Image.mData.length);
                            b.rewind();

                            if (mWaterMark != null) {
                                mNV12Image.blit(mRS, mWaterMark, 70, 70);
                            }

                            Allocation out = mProcessor.pipeProcess(mNV12Image);


                            mConverter.convert(mBitmap, out);
                            try {
                                Canvas c = mSurface.lockCanvas(null);
                                c.drawBitmap(mBitmap, mBmpRect, mRenderRect, null);
                                mSurface.unlockCanvasAndPost(c);
                                mDecoder.releaseOutputBuffer(idx, false);
                            }catch(IllegalStateException e) {
                                e.printStackTrace();
                            } catch (Surface.OutOfResourcesException e) {
                                e.printStackTrace();
                            }catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    */

    }

    public Surface getSurface() {
        return mSurface;
    }

    public void stop() {
        if (mState.getAndSet(-1) == 0) {
            return;
        }
        if (mDecoder != null) {
            mDecoder.stop();
            mDecoder.release();
        }
        if (mExtractor != null) {
            mExtractor.release();
        }
        if (mProcessor != null) {
            mProcessor.destroy();
        }
        if (mConverter != null) {
            mConverter.destroy();
        }

        if (mSurface != null) {
            mSurface.release();
        }

    }

    private ByteBuffer getInputBuffer(int idx) {
        if (Build.VERSION.SDK_INT <= 20) {
            if (mInputBuffers != null && idx >= 0 && idx < mInputBuffers.length) {
                return mInputBuffers[idx];
            }
            return null;
        } else {
            return mDecoder.getInputBuffer(idx);
        }
    }

    public ByteBuffer getOutputBuffer(int idx) {
        if (Build.VERSION.SDK_INT <= 20) {
            if (mOutputBuffers != null && idx >= 0 && idx < mOutputBuffers.length) {
                return mOutputBuffers[idx];
            }
            return null;
        } else {
            if (idx >= 0) {
                try {
                    return mDecoder.getOutputBuffer(idx);
                } catch (IllegalStateException e) {
                    return null;
                }
            }
            return null;
        }
    }

    private void fillInputBuffers() {
        int inIndex = mDecoder.dequeueInputBuffer(0);
        while(inIndex >= 0) {
            ByteBuffer buffer = getInputBuffer(inIndex);
            int sampleSize = mExtractor.readSampleData(buffer, 0);
            long sampleTime = mExtractor.getSampleTime();
            if (sampleSize < 0) {
                Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                mDecoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                break;
            } else {
                Log.v(TAG, "Fill input buffer: " + inIndex);
                mDecoder.queueInputBuffer(inIndex, 0, sampleSize, sampleTime, 0);
                mExtractor.advance();
            }
            inIndex = mDecoder.dequeueInputBuffer(0);
        }
    }

    private static int getFormatInteger(final MediaFormat fmt, final String name, final int defaultValue) {
        try {
            return fmt.getInteger(name);
        }
        catch (NullPointerException  e) { /* no such field */ }
        catch (ClassCastException e) { /* field of different type */ }
        return defaultValue;
    }
    private void initParameters(int bufferSize) {
        final MediaFormat fmt = mOutputFormat;
        int w = fmt.getInteger(MediaFormat.KEY_WIDTH);
        int h = fmt.getInteger(MediaFormat.KEY_HEIGHT);

        //Android Rect width doesn't include right, crop-right does. so + 1
        mBufferCrop.set(getFormatInteger(fmt, "crop-left", 0),
                        getFormatInteger(fmt, "crop-top", 0),
                        getFormatInteger(fmt, "crop-right", w-1),
                        getFormatInteger(fmt, "crop-bottom", h-1));

        Log.i(TAG, "mBufferCrop:" + mBufferCrop);
        int sliceHeight = getFormatInteger(fmt, "slice-height", h);
        if (sliceHeight <= 0)  {
            sliceHeight = h;
        }
        h = sliceHeight;
        int stride = getFormatInteger(fmt, "stride", w);
        if (stride < w) {
            stride = w;
        }
        w = stride;

        mBufferSize = bufferSize;
        mColorFormat = getFormatInteger(fmt, MediaFormat.KEY_COLOR_FORMAT,
                  MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        Log.i(TAG, "mBufferSize:" + mBufferSize);
        Log.i(TAG, "mColorFormat:" + mColorFormat);
        mBufferWidth = w;
        mBufferHeight = h;

        if (w * h * 3 /2 > bufferSize) {
            Log.w(TAG, String.format("buffer size %d from MediaFormat bigger than from BufferInfo %d", w*h*3/2, bufferSize));
        }
        int bufHeight = bufferSize * 2 / 3 / w;
        if (bufHeight != h && bufHeight > 0) {
            Log.w(TAG, String.format("Buffer size(%dx%d) not match real buffer size(%dx%d)",
                    w, h, w, bufHeight));
        }

        int vw = mBufferCrop.width();
        int vh = mBufferCrop.height();
        if (vw != mVideoWidth || vh != mVideoHeight) {
            Log.w(TAG, String.format("Video size from extractor (%dx%d) doesn't match decoded output(%dx%d) use decoded output",
                    mVideoWidth, mVideoHeight, vw, vh));
            mVideoWidth = vw;
            mVideoHeight = vh;
        }
        Log.i(TAG, "mBufferWidth:" + mBufferWidth);
        Log.i(TAG, "mBufferHeight:" + mBufferHeight);
        Log.i(TAG, "mVideoWidth:" + mVideoWidth);
        Log.i(TAG, "mVideoHeight:" + mVideoHeight);

    }

    private void initOutput(int bufferSize) {
        initParameters(bufferSize);

        int userCropedWidth = mVideoWidth;
        int userCropedHeight = mVideoHeight;

        if (mUserCrop != null) {
            userCropedWidth -= (int)(userCropedWidth * (mUserCrop.left + mUserCrop.right));
            userCropedHeight -= (int)(userCropedHeight * (mUserCrop.top + mUserCrop.bottom));
        }

        //align to 64 TODO: align to 64 on QCOMM tile output.
        int alignedVideoWidth =  Utils.align16(userCropedWidth);
        int alignedVideoHeight = Utils.align16(userCropedHeight);


        if (mRotation == 90 || mRotation == 270) {
            int t = alignedVideoWidth;
            alignedVideoWidth = alignedVideoHeight;
            alignedVideoHeight = t;
        }

        mNV12Image = new NV12Image(mBufferWidth, mBufferHeight);

        int surw = mSurRect.width();
        int surh = mSurRect.height();
        double videoRatio = ((double)alignedVideoWidth)/alignedVideoHeight;
        double surRatio = ((double)surw)/surh;

        if (videoRatio >= surRatio) {
            int rw = surw;
            int rh = (int)(rw / videoRatio);
            int rt = (surh - rh)/2;
            mRenderRect = new Rect(0, rt, rw, rt + rh);
        } else {
            int rh = surh;
            int rw = (int)(surh * videoRatio);
            int rl = (surw - rw)/2;
            mRenderRect = new Rect(rl, 0, rl + rw, rh);
        }

        int bmpw = Math.min(alignedVideoWidth, Utils.align16(mRenderRect.width()));
        int bmph = Math.min(alignedVideoHeight, Utils.align16(mRenderRect.height()));
        if (mRotation == 90 || mRotation == 270) {
            mProcessor = buildProcessor(mRS, bmph, bmpw, mRotation);
        } else {
            mProcessor = buildProcessor(mRS, bmpw, bmph, mRotation);
        }
        mConverter = new RsNV12ToARGB(mRS, bmpw, bmph);
        mBitmap = Bitmap.createBitmap(bmpw, bmph, Bitmap.Config.ARGB_8888);
        mBmpRect = new Rect(0, 0, bmpw, bmph);



        Log.i(TAG, "mRenderRect:" + mRenderRect);
        Log.i(TAG, "mBmpRect:" + mBmpRect);
    }

    public void seekTo(long timeUs, boolean renderNextFrame) {
        if (mState.get() <= 0) {
            throw new IllegalStateException("Decoder doesn't initialized");
        }
        mExtractor.seekTo(timeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        mDecoder.flush();
        mEos = false;
        if (renderNextFrame && mSurface != null) {

        }
    }
    public boolean isEos() {
        return mEos;
    }

    public int dequeueOutputBuffer(int timeout, MediaCodec.BufferInfo info) {
        if (mEos) {
            info.flags =  MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            return MediaCodec.INFO_TRY_AGAIN_LATER;
        }
        int outIndex = mDecoder.dequeueOutputBuffer(info, 0);
        if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER && !mEos) {
            fillInputBuffers();
            outIndex = mDecoder.dequeueOutputBuffer(info, timeout);
        }

        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.i(TAG, "See end of stream in decoding");
            mEos = true;
        }
        switch (outIndex) {
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                //{height=816, what=1869968451, color-format=2130706688, slice-height=816, crop-left=32, width=1408, crop-bottom=743, crop-top=24, mime=video/raw, stride=1408, crop-right=1311}
                //QCOM output QOMX_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka
                mOutputFormat = mDecoder.getOutputFormat();
                Log.i(TAG, "Output format:" + mOutputFormat); //ITU get this
                mState.set(2);
                break;
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                //  Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
                break;
            default:
                if (Build.VERSION.SDK_INT <= 20 && outIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    mOutputBuffers = mDecoder.getOutputBuffers();
                }
                if (outIndex >= 0 && mState.get() < 3) {
                    mState.set(3);
                    initOutput(info.size);
                }
                Log.v(TAG, "got output buffer index " + outIndex);
                break;
        }

        return outIndex;
    }
    int mRotation;
    int mVideoRotation;
    void detectRotation(Context ctx, Uri uri) {

        MediaMetadataRetriever m = new MediaMetadataRetriever();
        m.setDataSource(ctx, uri);
        String sRot = null;
        if (Build.VERSION.SDK_INT >= 17) {
            sRot = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        } else {
            sRot = m.extractMetadata(24);
        }
        if (sRot != null) {
            try {
                int rot = Integer.parseInt(sRot);
                Log.i(TAG, "rotation = " + sRot);
                if (rot == 0 || rot == 90 || rot == 180 || rot == 270) {
                    mVideoRotation = rot;
                    return;
                }
            } catch (NumberFormatException e) {
            }
        }
        m.release();

        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(ctx, uri);
            mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    if (width < height) {
                        mVideoRotation = 90;
                    }
                }
            });
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.release();
    }
    RectF mUserCrop;

    // left, top, right, bottom are ratio
    void setUserCrop(RectF crop) {
        if (mState.get() != 0) {
            throw new IllegalStateException("Decoder initialized");
        }
        if (crop != null) {
            mUserCrop = new RectF(crop);
        } else {
            mUserCrop = null;
        }
    }
    int mUserRotation;
    void setUserRotation(int rot) {
        if (mState.get() != 0) {
            throw new IllegalStateException("Decoder initialized");
        }
        mUserRotation = rot;
    }

    int mMirror;
    void setMirror(int mirror) {
        mMirror = mirror;
    }
    void setDataSource(Context ctx, Uri uri) throws IOException {
        if (mState.get() != 0) {
            throw new IllegalStateException("decoder already initialized");
        }
        mExtractor = new MediaExtractor();
        mExtractor.setDataSource(ctx, uri, null);

        detectRotation(ctx, uri);

        if (mVideoRotation != 0 && mUserCrop != null) {
            mUserCrop = Utils.rotateRectF(mUserCrop, 360 - mVideoRotation);
        }
        mRotation = (mVideoRotation + mUserRotation)%360;
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            MediaFormat format = mExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                mExtractor.selectTrack(i);
                mDecoder = MediaCodec.createDecoderByType(mime);
             //   mDecoder.setCallback(null);
                Log.i(TAG, "Video format: " + format);
                mInputFormat = format;
                //TODO enumlate decoder output format and select prefered
               // format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
                mDecoder.configure(format, null, null, 0);
                mVideoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                mVideoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                mDuration = format.getLong(MediaFormat.KEY_DURATION);
                break;
            }
        }
        if (mDecoder == null) {
            throw new IOException("No video track found in the input " + uri);
        }
        mDecoder.start();
        if (Build.VERSION.SDK_INT <= 20) {
            mInputBuffers = mDecoder.getInputBuffers();
            mOutputBuffers = mDecoder.getOutputBuffers();
        }
        mState.set(1);
    }
}
