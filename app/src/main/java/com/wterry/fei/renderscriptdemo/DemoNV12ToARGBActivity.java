package com.wterry.fei.renderscriptdemo;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v8.renderscript.RenderScript;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class DemoNV12ToARGBActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_nv12_to_argb);

        NV12Image nv12Image = new NV12Image(1280, 720, Utils.loadRawResource(this, R.raw.frame_720p));

        Bitmap bmp = nv12Image.toBitmap(RenderScript.create(this));
        ((ImageView)this.findViewById(R.id.imageView)).setImageBitmap(bmp);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demo_nv12_to_argb, menu);
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
