package com.wterry.fei.renderscriptdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v8.renderscript.RenderScript;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class DemoARGBToNV12Activity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_argbto_nv12);

        final Bitmap bmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.test);
        RenderScript rs = RenderScript.create(this);

        final NV12Image nv12Image = new NV12Image(rs, bmp);
        ((ImageView)findViewById(R.id.imageView)).setImageBitmap(nv12Image.toBitmap(rs));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demo_argbto_nv12, menu);
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
