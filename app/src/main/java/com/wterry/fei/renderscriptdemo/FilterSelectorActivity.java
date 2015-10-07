package com.wterry.fei.renderscriptdemo;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;


public class FilterSelectorActivity extends ActionBarActivity {

    public static final String TAG_SRC_WIDTH = "src-width";
    public static final String TAG_SRC_HEIGHT = "src-height";
    public static final String TAG_CROP_LEFT = "crop-left";
    public static final String TAG_CROP_TOP =  "crop-top";
    public static final String TAG_CROP_RIGHT = "crop-right";
    public static final String TAG_CROP_BOTTOM = "crop-bottom";
    public static final String TAG_DEST_WIDTH = "dest-width";
    public static final String TAG_DEST_HEIGHT= "dest-height";
    public static final String TAG_ROTATION = "rotation";
    public static final String TAG_MIRROR= "mirror";



    private void setSpinder(int resid, int min, int max, int step, int def) {
        Spinner s = (Spinner)findViewById(resid);
        int pos = 0;
        ArrayList<CharSequence> ar = new ArrayList<CharSequence>();
        for (int i = min; i < max; i+=step) {
            ar.add(i+"");
            if (i == def) {
                pos = i;
            }
        }
        ArrayAdapter<CharSequence> a = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ar);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a);
        s.setSelection((pos-min)/step);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_selector);

        int srcWidth = this.getIntent().getIntExtra(TAG_SRC_WIDTH, 1280);
        int srcHeight = this.getIntent().getIntExtra(TAG_SRC_HEIGHT, 720);

        setSpinder(R.id.crop_left_spinner, 0, srcWidth/2-8, 2, 0);
        setSpinder(R.id.crop_top_spinner, 0, srcHeight/2-8, 2, 0);
        setSpinder(R.id.crop_right_spinner,  srcWidth/2+9, srcWidth, 2, srcWidth-1);
        setSpinder(R.id.crop_bottom_spinner,  srcHeight/2+9, srcHeight, 2, srcHeight-1);

        setSpinder(R.id.dest_width_spinner,  16, srcWidth+1, 4, srcWidth);
        setSpinder(R.id.dest_height_spinner,  16, srcHeight+1, 4, srcHeight);

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


        this.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();

                String rot = (String)((Spinner) findViewById(R.id.rotation_spinner)).getSelectedItem();
                String mir = (String)((Spinner) findViewById(R.id.mirror_spinner)).getSelectedItem();

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

                String cropLeft = (String)((Spinner) findViewById(R.id.crop_left_spinner)).getSelectedItem();
                String cropRight = (String)((Spinner) findViewById(R.id.crop_right_spinner)).getSelectedItem();
                String cropTop = (String)((Spinner) findViewById(R.id.crop_top_spinner)).getSelectedItem();
                String cropBottom = (String)((Spinner) findViewById(R.id.crop_bottom_spinner)).getSelectedItem();

                String dstWidth = (String)((Spinner) findViewById(R.id.dest_width_spinner)).getSelectedItem();
                String dstHeight = (String)((Spinner) findViewById(R.id.dest_height_spinner)).getSelectedItem();

                int ndh = Integer.parseInt(dstHeight);
                int ndw = Integer.parseInt(dstWidth);

                int cl = Integer.parseInt(cropLeft);
                int ct = Integer.parseInt(cropTop);
                int cr = Integer.parseInt(cropRight);
                int cb = Integer.parseInt(cropBottom);


                data.putExtra(TAG_CROP_LEFT, cl);
                data.putExtra(TAG_CROP_TOP, ct);
                data.putExtra(TAG_CROP_RIGHT, cr);
                data.putExtra(TAG_CROP_BOTTOM, cb);
                data.putExtra(TAG_DEST_WIDTH, ndw);
                data.putExtra(TAG_DEST_HEIGHT, ndh);
                data.putExtra(TAG_ROTATION, nrot);
                data.putExtra(TAG_MIRROR, nmir);

                FilterSelectorActivity.this.setResult(RESULT_OK, data);

                FilterSelectorActivity.this.finish();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_filter_selector, menu);
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
