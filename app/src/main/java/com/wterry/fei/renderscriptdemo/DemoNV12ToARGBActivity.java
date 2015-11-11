package com.wterry.fei.renderscriptdemo;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v8.renderscript.RenderScript;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


public class DemoNV12ToARGBActivity extends ActionBarActivity {

    static final int mWidth = 1280;
    static final int mHeight = 720;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_nv12_to_argb);
        NV12Image nv12Image = new NV12Image(mWidth, mHeight, Utils.loadRawResource(this, R.raw.frame_720p));
        Bitmap bmp = nv12Image.toBitmap(RenderScript.create(this));
        ((ImageView)this.findViewById(R.id.imageView)).setImageBitmap(bmp);

        findViewById(R.id.but_testrs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testRenderScriptConverter();
            }
        });

        findViewById(R.id.but_testsoftware).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testSoftwareConverter();
            }
        });
    }

    public void testSoftwareConverter() {
        final byte [] nv12Data =  Utils.loadRawResource(this, R.raw.frame_720p);
        final int [] out = new int[mWidth * mHeight];
        new AsyncTask<Object, Object, Long>(){
            @Override
            protected Long doInBackground(Object... params) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < 100; i++) {
                    Utils.NV12ToARGB(nv12Data, out, mWidth, mHeight);
                }
                return System.currentTimeMillis() - start;
            }
            @Override
            protected void onPostExecute(Long result) {
                Bitmap bmp = Bitmap.createBitmap(out, mWidth, mHeight, Bitmap.Config.ARGB_8888);
                        ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bmp);
                Toast.makeText(DemoNV12ToARGBActivity.this,
                        "Software convert use " + result + " Mills" + " FPS = " + (100000L/result),
                        Toast.LENGTH_LONG).show();
            }
        }.execute();

    }
    public void testRenderScriptConverter() {
        final byte [] nv12Data = Utils.loadRawResource(this, R.raw.frame_720p);
        final Bitmap out =  Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        final RsNV12ToARGB converter = new RsNV12ToARGB(RenderScript.create(this), mWidth, mHeight);
        new AsyncTask<Object, Object, Long>(){
            @Override
            protected Long doInBackground(Object... params) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < 100; i++) {
                    converter.convert(out, nv12Data);
                }
                return System.currentTimeMillis() - start;
            }
            @Override
            protected void onPostExecute(Long result) {
                ((ImageView)findViewById(R.id.imageView)).setImageBitmap(out);
                Toast.makeText(DemoNV12ToARGBActivity.this,
                        "RS convert use " + result + " Mills" + " FPS = " + (100000L/result),
                        Toast.LENGTH_LONG).show();
            }
        }.execute();
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
