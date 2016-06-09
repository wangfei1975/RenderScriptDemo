package com.wterry.fei.renderscriptdemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.io.IOException;

public class DemoVideoPostProcessingActivity extends ActionBarActivity {

    static final String TAG = DemoVideoPostProcessingActivity.class.getSimpleName();
    private GLVideoRender mPreview;
    private VideoDecoder mDecoder;
    private SurfaceTexture mSurfaceTexture;
    private RenderScript rs ;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private CropSelectorView mCropSelector;

    private void reLayout() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ABOVE, R.id.layout_rot_mirror);
        lp.setMargins(5, 10, 5, 10);
        mPreview.setLayoutParams(lp);
    }
    static final Uri mVideoUri = Uri.parse("android.resource://com.wterry.fei.renderscriptdemo/" + R.raw.bunny720p);
    Bitmap mWaterMark;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_video_post_processing);

        mPreview = (GLVideoRender) findViewById(R.id.videoView);
        mCropSelector = (CropSelectorView) findViewById(R.id.cropSelectorView);
        mCropSelector.bringToFront();

        Spinner spinner = (Spinner) findViewById(R.id.rotation_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.rotation_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        Spinner spinner1 = (Spinner) findViewById(R.id.mirror_spinner);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.mirror_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);



        mWaterMark = Bitmap.createBitmap(880, 100, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(mWaterMark);
        c.drawARGB(70, 0, 0, 0);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(255, 20, 20));
        paint.setTextSize(80);
        paint.setStyle(Paint.Style.FILL);
        c.drawText("Water mark demo", 10, 80, paint);

        findViewById(R.id.but_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String rot = (String) ((Spinner) findViewById(R.id.rotation_spinner)).getSelectedItem();
                final String mir = (String) ((Spinner) findViewById(R.id.mirror_spinner)).getSelectedItem();


                mCropSelector.setVisibility(View.GONE);
                mPreview.bringToFront();
                final VideoDecoder decoder = new VideoDecoder(rs, mPreview, mSurfaceWidth, mSurfaceHeight);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int nrot = 0;
                            if (rot.equals("Clockwise 90")) {
                                nrot = 90;
                            } else if (rot.equals("180")) {
                                nrot = 180;
                            } else if (rot.equals("Clockwise 270")) {
                                nrot = 270;
                            }
                            int nmir = 0;
                            if (mir.equals("Flip Horizontal")) {
                                nmir = 1;
                            } else if (mir.equals("Flip Vertical")) {
                                nmir = 2;
                            }
                            decoder.setWaterMark(mWaterMark);
                            decoder.setUserCrop(mCropSelector.getUserCrop());
                            decoder.setMirror(nmir);
                            decoder.setUserRotation(nrot);
                            decoder.setDataSource(DemoVideoPostProcessingActivity.this, mVideoUri);
                            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                            while (mSurfaceTexture != null) {
                                int idx = decoder.dequeueOutputBuffer(0, info);
                                if (idx >= 0) {
                                    decoder.releaseOutputBuffer(idx, mSurfaceTexture != null);
                                }

                                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                    break;
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        decoder.stop();
                    }
                }).start();

            }
        });

        mPreview.setVideoSize(1280, 720);
        mPreview.setListener(new GLVideoRender.Listener() {
            @Override
            public void onSurfaceCreated(SurfaceTexture surface, final int width, final int height) {

                mSurfaceTexture = surface;
                //  mSurface = new Surface(surface);
                mSurfaceWidth = width;
                mSurfaceHeight = height;
                DemoVideoPostProcessingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ViewGroup.LayoutParams lp = mCropSelector.getLayoutParams();
                        lp.width = width;
                        lp.height = height;

                        mCropSelector.setLayoutParams(lp);


                        // mVideoEditView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
                        initDecoder();
                    }
                });





                /*
                Log.i(TAG, "surface available width = " + width + " height = " + height);
                try {
                    Uri uri = mSegments.get(0).mUri;
                     FileDescriptor fd =  getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor();
                    final Bitmap bmp =  BitmapFactory.decodeFileDescriptor(fd);
                final Bitmap bmp1 = BitmapFactory.decodeResource(VideoEditActivity.this.getResources(), R.drawable.editor_icon_draw_press);
                //final Paint pt =  new Paint(Paint.ANTI_ALIAS_FLAG);
                final Surface sur = new Surface(surface);
                final Rect sr = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
                final Rect r = new Rect(0, 0, width, height);
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < 200; i++) {
                            Canvas c = sur.lockCanvas(r);


                            //  Canvas c2 = holder.lockCanvas();
                            //c.drawBitmap(bmp, 0, 0, null);
                            c.drawBitmap(bmp, sr, r, null);
                            c.drawBitmap(bmp1, 0, 0, null);
                            //  Log.i(TAG, "Canvas is hardwareAccelerated : " + c.isHardwareAccelerated());
                            // c2.drawBitmap(bmp, 0, 0, pt);
                            sur.unlockCanvasAndPost(c);
                            // holder.unlockCanvasAndPost(c2);
                            Log.i(TAG, "Canvas is hardwareAccelerated : " + c.isHardwareAccelerated());

                        }
                        long end = System.currentTimeMillis();
                        Log.i(TAG, "draw 200 time use " + (end - start) + " ms");
                    }
                }).start();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                */
            }

            @Override
            public void onSurfaceDestroyed(SurfaceTexture surface) {

            }
/*
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                mSurfaceTexture = surface;
                //   mSurface = new Surface(surface);
                mSurfaceWidth = width;
                mSurfaceHeight = height;
                Log.i(TAG, "surface View size changed width = " + width + " height = " + height);

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mSurfaceTexture = null;

                //  mSurface = null;
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                //  mSurfaceTexture = surface;

            }
             */
        });

    }

    private void initDecoder() {
        final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        if (rs == null) {
            rs = RenderScript.create(this);
        }
        mDecoder = new VideoDecoder(rs, mPreview, mSurfaceWidth, mSurfaceHeight);


        //final Bitmap bmp  = BitmapFactory.decodeResource(VideoEditActivity.this.getResources(), R.drawable.editor_icon_draw_press);
        try {
            mDecoder.setDataSource(DemoVideoPostProcessingActivity.this, mVideoUri);
            final Bitmap bb = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
            mDecoder.getThumbnail(bb, 2000000L);
           // int w = mDecoder.getRenderRect().width();
           // int h = mDecoder.getRenderRect().height();
          //  ViewGroup.LayoutParams lp = new   ViewGroup.LayoutParams(w, h);
           // mPreview.setLayoutParams(lp);


        } catch (IOException e) {
            e.printStackTrace();
            return;
        }



        new Thread(new Runnable(){
            @Override
            public void run() {


                mCropSelector.setVideoRect(mDecoder.getRenderRect());

                mDecoder.seekTo(0, false);


                long dur = mDecoder.getDuration();

                long start = System.currentTimeMillis();
                Log.i(TAG, "decode start  " + start);
                while (mSurfaceTexture != null) {

                    int idx = mDecoder.dequeueOutputBuffer(0, info);
                    if (idx >= 0) {
                        Log.v(TAG, "render buffer :" + idx);
                        mDecoder.releaseOutputBuffer(idx, mSurfaceTexture != null);

                        //  if (info.presentationTimeUs >= 8000000L) {

                        //    break;
                        // }

                        if (info.presentationTimeUs > 0) {
                            // mSeekBar.setProgress((int)(info.presentationTimeUs/1000000L));
                        }

                    }

                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.i(TAG, "end of stream");
                        break;
                    }


                    Log.i(TAG, "decode end dur  " + (System.currentTimeMillis() - start));
                    //    mDecoder.stop();
                    //  mSeekBar.setProgress(mSeekBar.getMax());

                }
            }
        }).start();

    }
}
