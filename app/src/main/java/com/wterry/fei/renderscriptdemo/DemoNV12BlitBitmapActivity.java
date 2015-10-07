package com.wterry.fei.renderscriptdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v8.renderscript.RenderScript;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class DemoNV12BlitBitmapActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_nv12_blit_bitmap);

        int srcWidth = 1280;
        int srcHeight = 720;

        NV12Image nv12Image = new NV12Image(srcWidth, srcHeight, Utils.loadRawResource(this, R.raw.frame_720p));
        RenderScript rs = RenderScript.create(this);
        ((ImageView)this.findViewById(R.id.imageViewSrc)).setImageBitmap(nv12Image.toBitmap(rs));

        final Bitmap bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.tux), 240, 240, false);
         nv12Image.blit(rs, bmp, 10, 10);

        Bitmap b1 = Bitmap.createBitmap(880, 100, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b1);
        c.drawARGB(70, 0, 0, 0);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(255, 20, 20));
        paint.setTextSize(80);
        paint.setStyle(Paint.Style.FILL);
        String s = getResources().getString(R.string.demoNV12BlitBitmap);
        c.drawText(s, 10, 80, paint);
        nv12Image.blit(rs, b1, 300, 180);
        ((ImageView) this.findViewById(R.id.imageViewDst)).setImageBitmap(nv12Image.toBitmap(rs));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demo_nv12_blit_bitmap, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
