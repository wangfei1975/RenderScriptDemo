package com.wterry.fei.renderscriptdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by feiwang on 15-04-02.
 */
public class VideoView extends TextureView {
    private static final int IMAGES_PER_FRAME = 100;
    private static final long FPS_CALC_INTERVAL = 1000L;

    private Bitmap bitmap;
    private Paint paint;

    private long lastFpsCalcUptime;
    private long frameCounter;

    private long fps;

    public VideoView(Context context) {
        this(context, null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onResume() {
        prepare();
    }

    public void onPause() {
    }

    /*
    @Override
    protected void onDraw(Canvas canvas) {

        measureFps();

       // canvas.drawColor(Color.RED);

       // for (int i = 0; i < 1; i++) {
          //  int x = (int) (Math.random() * (canvas.getWidth() - bitmap.getWidth()));
          //  int y = (int) (Math.random() * (canvas.getHeight() - bitmap.getHeight()));
            canvas.drawBitmap(bitmap, 0, 0, null);
       // }

        canvas.drawText("fps=" + fps, 0, 30, paint);

        boolean isViewAccelerated = false;
        boolean isCanvasAccelerated = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // This code must not be executed on a device with API level less than 11 (Android 2.x, 1.x)
            isViewAccelerated = isHardwareAccelerated();
            isCanvasAccelerated = canvas.isHardwareAccelerated();
        }
        canvas.drawText("isViewAccelerated=" + isViewAccelerated, 0, 60, paint);
        canvas.drawText("isCanvasAccelerated=" + isCanvasAccelerated, 0, 75, paint);
    }
    */
    private void measureFps() {
        frameCounter++;
        long now = SystemClock.uptimeMillis();
        long delta = now - lastFpsCalcUptime;
        if (delta > FPS_CALC_INTERVAL) {
            fps = frameCounter * FPS_CALC_INTERVAL / delta;

            frameCounter = 0;
            lastFpsCalcUptime = now;
        }
    }

    private void prepare() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);

        paint = new Paint();
        paint.setDither(true);
        paint.setColor(Color.WHITE);

        lastFpsCalcUptime = SystemClock.uptimeMillis();
        frameCounter = 0;
    }

}
