package com.wterry.fei.renderscriptdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v8.renderscript.RenderScript;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;


public class DemoNV12ImageJointActivity extends ActionBarActivity {

    RenderScript mRS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_nv12_image_processor);
        final int srcWidth = 1280;
        final int srcHeight = 720;
        NV12Image nv12Image = new NV12Image(srcWidth, srcHeight, Utils.loadRawResource(this, R.raw.frame_720p));
        mRS = RenderScript.create(this);
        ((ImageView)this.findViewById(R.id.imageViewSrc)).setImageBitmap(nv12Image.toBitmap(mRS));

        int cropLeft = 300;
        int cropTop = 100;
        int cropRight  = srcWidth-100;
        int cropBottom = srcHeight-200;

        int dstWidth =   640;
        int dstHeight =  480;

        int dstx = 400;
        int dsty = 40;
        int strideX = 1280;
        int strideY = 720;

        int rotation = 270;
        int mirror =   0;



        RsNV12ImageJointer processor = new RsNV12ImageJointer.Builder(mRS)
                .setSrcSize(srcWidth, srcHeight)
                .setDstSize(dstWidth, dstHeight)
                .setCrop(cropLeft, cropTop, cropRight, cropBottom)
                .setDstOffset(dstx, dsty)
                .setStride(strideX, strideY)
                .setMirror(mirror)
                .setRotation(rotation).build();

        processor.setBackground(nv12Image);
        NV12Image nnv12Image = processor.process(nv12Image);
     //   Bitmap outBmp = Utils.NV12ToBitmap(nnv12Image.getData(), dstWidth, dstHeight);//nnv12Image.toBitmap(mRS);

        Bitmap outBmp = nnv12Image.toBitmap(mRS);

        ((ImageView)this.findViewById(R.id.imageViewDst)).setImageBitmap(outBmp);


        this.findViewById(R.id.but_select_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DemoNV12ImageJointActivity.this, FilterSelectorActivity.class);
                i.putExtra(FilterSelectorActivity.TAG_SRC_WIDTH, srcWidth);
                i.putExtra(FilterSelectorActivity.TAG_SRC_HEIGHT, srcHeight);
                startActivityForResult(i, 100);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {


            Bundle bd = data.getExtras();
            int srcWidth = 1280;
            int srcHeight = 720;
            int cropLeft = bd.getInt(FilterSelectorActivity.TAG_CROP_LEFT, 0);
            int cropTop = bd.getInt(FilterSelectorActivity.TAG_CROP_TOP, 0);
            int cropRight = bd.getInt(FilterSelectorActivity.TAG_CROP_RIGHT, srcWidth-1);
            int cropBottom = bd.getInt(FilterSelectorActivity.TAG_CROP_BOTTOM, srcHeight-1);

            int dstWidth = bd.getInt(FilterSelectorActivity.TAG_DEST_WIDTH, srcWidth);
            int dstHeight = bd.getInt(FilterSelectorActivity.TAG_DEST_HEIGHT, srcHeight);
            int rotation = bd.getInt(FilterSelectorActivity.TAG_ROTATION, 0);
            int mirror = bd.getInt(FilterSelectorActivity.TAG_MIRROR, 0);

            NV12Image nv12Image = new NV12Image(srcWidth, srcHeight,
                    Utils.loadRawResource(this, R.raw.frame_720p));


            RsNV12ImageProcessor processor = new RsNV12ImageProcessor.Builder(mRS)
                    .setSrcSize(srcWidth, srcHeight)
                    .setDstSize(dstWidth, dstHeight)
                    .setCrop(cropLeft, cropTop, cropRight, cropBottom)
                    .setMirror(mirror)
                    .setRotation(rotation).build();

            NV12Image nnv12Image = processor.process(nv12Image);
            Bitmap outBmp = nnv12Image.toBitmap(mRS);

            ((ImageView)this.findViewById(R.id.imageViewDst)).setImageBitmap(outBmp);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demo_nv12_image_processor, menu);
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
