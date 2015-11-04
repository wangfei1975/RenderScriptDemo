package com.wterry.fei.renderscriptdemo;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.findViewById(R.id.but_demo_NV12ToARGB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DemoNV12ToARGBActivity.class);
                startActivity(i);
            }
        });

        this.findViewById(R.id.but_demo_ARGBToNV12).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DemoARGBToNV12Activity.class);
                startActivity(i);
            }
        });
        this.findViewById(R.id.but_demo_NV12ImageProcessor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DemoNV12ImageProcessorActivity.class);
                startActivity(i);
            }
        });
        this.findViewById(R.id.but_demo_NV12BlitBitmap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DemoNV12BlitBitmapActivity.class);
                startActivity(i);
            }
        });

        this.findViewById(R.id.but_demo_VideoPostProcessing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DemoVideoPostProcessingActivity.class);
                startActivity(i);
            }
        });
/*
        byte []jpgd = Utils.loadRawResource(this, R.raw.tttt);


        Bitmap bmp = BitmapFactory.decodeByteArray(jpgd, 0, jpgd.length);

        ((ImageView)findViewById(R.id.image)).setImageBitmap(bmp);

        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            BitmapFactory.decodeByteArray(jpgd, 0, jpgd.length);
           // bmp.recycle();

        }

        long end = System.currentTimeMillis();
        Log.i("test bitmp", "use " + (end - start) + " ms");
*/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
