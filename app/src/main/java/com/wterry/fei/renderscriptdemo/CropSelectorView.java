package com.wterry.fei.renderscriptdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Fei Wang on 10/20/2015.
 */
public class CropSelectorView extends View {
    static final String TAG = CropSelectorView.class.getSimpleName();
    Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint mOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    void initPaints() {
        // mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        // mPaint.setARGB(0xA0, 0, 0, 0);
        mPaint.setColor(Color.WHITE);
        mPaint.setAlpha(0xFF);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        mOuterPaint.setARGB(0xa0, 0, 0, 0);
        mOuterPaint.setStyle(Paint.Style.FILL);
        // mOuterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public CropSelectorView(Context context) {
        super(context);

        initPaints();
    }

    public CropSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    public CropSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initPaints();
    }

    public RectF getUserCrop() {
        float w = mVideoRenderRect.width();
        float h = mVideoRenderRect.height();
        return new RectF((mCropRect.left - mVideoRenderRect.left)/w,
                (mCropRect.top - mVideoRenderRect.top)/h,
                (mVideoRenderRect.right - mCropRect.right)/w,
                (mVideoRenderRect.bottom - mCropRect.bottom)/h);
    }
    public void setVideoRect(Rect vr) {
        mVideoRenderRect = vr;
        mCropRect = new Rect(vr.left+RESIZE_HANDLE_WIDTH, vr.top+RESIZE_HANDLE_WIDTH, vr.right-RESIZE_HANDLE_WIDTH, vr.bottom-RESIZE_HANDLE_WIDTH);
        calcResizeHandleRects(mCropRect, mResizeHandleRects);
    }


    static final int RESIZE_HANDLE_WIDTH = 35;
    private static void calcResizeHandleRects(Rect r, Rect [] rs) {
        rs[0].set(r.left - RESIZE_HANDLE_WIDTH, r.top - RESIZE_HANDLE_WIDTH, r.left + RESIZE_HANDLE_WIDTH, r.top + RESIZE_HANDLE_WIDTH);
        rs[1].set(r.right - RESIZE_HANDLE_WIDTH, r.top - RESIZE_HANDLE_WIDTH, r.right + RESIZE_HANDLE_WIDTH, r.top + RESIZE_HANDLE_WIDTH);
        rs[2].set(r.left - RESIZE_HANDLE_WIDTH, r.bottom - RESIZE_HANDLE_WIDTH, r.left + RESIZE_HANDLE_WIDTH, r.bottom + RESIZE_HANDLE_WIDTH);
        rs[3].set(r.right - RESIZE_HANDLE_WIDTH, r.bottom - RESIZE_HANDLE_WIDTH, r.right + RESIZE_HANDLE_WIDTH, r.bottom + RESIZE_HANDLE_WIDTH);
    }

    Rect mVideoRenderRect;
    Rect mCropRect;
    Rect [] mResizeHandleRects = new Rect[] {new Rect(), new Rect(), new Rect(), new Rect()};
    int mOperation = 0;
    int mResizingX, mResizingY;

    static final int MIN_WIDTH = 64;
    static final int MIN_HEIGHT= 64;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        // Log.d(TAG, String.valueOf(action));
        int x, y, dx, dy;
        Rect tr = new Rect();
        switch (action) {

            case MotionEvent.ACTION_DOWN:
                x = (int)e.getX();
                y = (int)e.getY();
                mOperation = 0;
                for (int i = 0; i < mResizeHandleRects.length; i++) {
                    if (mResizeHandleRects[i].contains(x, y)) {
                        mOperation = i+1;
                        mResizingX = x;
                        mResizingY = y;
                        Log.v(TAG, "resize start...");
                        break;
                    }
                }
                if (mOperation == 0 && mCropRect.contains(x, y)) {
                    Log.v(TAG, "move start...");
                    mResizingX = x;
                    mResizingY = y;
                    mOperation = 5;
                    return true;
                }
                if (mOperation > 0) {
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:

                x = (int)e.getX();
                y = (int)e.getY();
                Log.v(TAG, "action mov op:" + mOperation);
                switch (mOperation) {
                    case 1:
                        dx = x - mResizingX;
                        dy = y - mResizingY;
                        if (dx != 0|| dy != 0) {
                            tr.set(mCropRect.left + dx, mCropRect.top + dy, mCropRect.right, mCropRect.bottom);
                            if (mVideoRenderRect.contains(tr) && tr.width() > MIN_WIDTH && tr.height() > MIN_HEIGHT) {
                                mResizingX = x;
                                mResizingY = y;
                                mCropRect.set(tr);
                                calcResizeHandleRects(tr, mResizeHandleRects);
                                this.invalidate();

                            }
                        }
                        return true;
                    case 2:
                        dx = x - mResizingX;
                        dy = y - mResizingY;
                        if (dx != 0|| dy != 0) {
                            tr.set(mCropRect.left, mCropRect.top+dy, mCropRect.right+dx, mCropRect.bottom);
                            if (mVideoRenderRect.contains(tr) && tr.width() > MIN_WIDTH && tr.height() > MIN_HEIGHT) {
                                mResizingX = x;
                                mResizingY = y;
                                mCropRect.set(tr);
                                calcResizeHandleRects(tr, mResizeHandleRects);
                                this.invalidate();

                            }
                        }
                        return true;
                    case 3:
                        dx = x - mResizingX;
                        dy = y - mResizingY;
                        if (dx != 0|| dy != 0) {
                            tr.set(mCropRect.left+dx, mCropRect.top, mCropRect.right, mCropRect.bottom+dy);
                            if (mVideoRenderRect.contains(tr) && tr.width() > MIN_WIDTH && tr.height() > MIN_HEIGHT) {
                                mResizingX = x;
                                mResizingY = y;
                                mCropRect.set(tr);
                                calcResizeHandleRects(tr, mResizeHandleRects);
                                this.invalidate();

                            }
                        }
                        break;
                    case 4:
                        dx = x - mResizingX;
                        dy = y - mResizingY;
                        if (dx != 0|| dy != 0) {
                            tr.set(mCropRect.left, mCropRect.top, mCropRect.right+dx, mCropRect.bottom+dy);
                            if (mVideoRenderRect.contains(tr) && tr.width() > MIN_WIDTH && tr.height() > MIN_HEIGHT) {
                                mResizingX = x;
                                mResizingY = y;
                                mCropRect.set(tr);
                                calcResizeHandleRects(tr, mResizeHandleRects);
                                this.invalidate();

                            }
                        }
                        return true;
                    case 5:
                        dx = x - mResizingX;
                        dy = y - mResizingY;

                        if (dx != 0 || dy != 0) {
                            if (mCropRect.left + dx < mVideoRenderRect.left) {
                                dx = mVideoRenderRect.left - mCropRect.left;
                            } else if (mCropRect.right + dx > mVideoRenderRect.right) {
                                dx = mVideoRenderRect.right - mCropRect.right;
                            }

                            if (mCropRect.top + dy < mVideoRenderRect.top) {
                                dy = mVideoRenderRect.top - mCropRect.top;
                            } else if (mCropRect.bottom + dy > mVideoRenderRect.bottom) {
                                dy = mVideoRenderRect.bottom - mCropRect.bottom;
                            }

                            if (dx != 0 || dy != 0) {
                                mResizingX = x;
                                mResizingY = y;
                                mCropRect.offset(dx, dy);
                                calcResizeHandleRects(mCropRect, mResizeHandleRects);
                                this.invalidate();

                            }
                        }
                        return true;
                    default:
                        break;
                }


                break;

            case MotionEvent.ACTION_UP:
                float finalX = e.getX();
                float finalY = e.getY();

                Log.d(TAG, "Action was UP");

                mOperation = 0;
                break;

            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG,"Action was CANCEL");
                mOperation = 0;
                break;

            case MotionEvent.ACTION_OUTSIDE:
                Log.d(TAG, "Movement occurred outside bounds of current screen element");
                mOperation = 0;
                break;
        }

        return super.onTouchEvent(e);
    }
    Path p = new Path();
    @Override
    protected void onDraw(Canvas cs) {

        //super.onDraw(cs);

        if (mCropRect != null) {

            int w = getMeasuredWidth();
            int h = getMeasuredHeight();
            //   Log.i(TAG, "onDraw mCropRect " + mCropRect);
            cs.drawRect(0, 0, w, mCropRect.top, mOuterPaint);
            cs.drawRect(0, mCropRect.top, mCropRect.left, mCropRect.bottom, mOuterPaint);
            cs.drawRect(0, mCropRect.bottom, w, h, mOuterPaint);
            cs.drawRect(mCropRect.right, mCropRect.top, w, mCropRect.bottom, mOuterPaint);

            // cs.drawRect(mCropRect, mInnerPaint);
            cs.drawRect(mCropRect, mPaint);
            //cs.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mOuterPaint);


            //p.moveTo(mCropRect.left, mCropRect.top);
            //    p.moveTo(mCropRect.left, mCropRect.bottom);
            //    p.moveTo(mCropRect.right, mCropRect.bottom);
            //    p.moveTo(mCropRect.right, mCropRect.top);
            //p.close();
//p.moveTo(mCropRect.right, mCropRect.top);
            //p.setFillType(Path.FillType.INVERSE_EVEN_ODD);

            // cs.clipRect(mCropRect);
            // cs.drawColor(Color.TRANSPARENT, PorterDuff.Mode.ADD);
            //cs.drawPath(p, mOuterPaint);
            //  cs.drawRect(mCropRect, mPaint);
        }
    }
}
