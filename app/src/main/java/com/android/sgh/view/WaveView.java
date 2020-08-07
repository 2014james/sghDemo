package com.android.sgh.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.android.sgh.R;
import com.android.sgh.util.DensityUtil;

/***
 *Created by sgh
 *on 2020\5\27 0027
 *
 */
public class WaveView extends View {

    String TAG = "WaveView";

    /**
     * 画笔
     */
    private Paint whilePaint, blackPaint;

    private Canvas mCacheCanvas;

    private Bitmap mCacheBitMap;
    private Rect mShowRect;
    private Rect rect;

    /**
     * 画第几个线
     */
    private int index;

    /**
     * 全屏能画多少个
     */
    private int maxLinPreWidth;

    /**
     * 缓冲画的个数，2个屏幕
     */
    private int cacheCount;

    /**
     * 每6像素画一个线
     */
    private int perPix = 6;

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        whilePaint = new Paint();
        whilePaint.setColor(getResources().getColor(R.color.white));
        whilePaint.setStrokeWidth(1.0f);

        blackPaint = new Paint();
        blackPaint.setColor(getResources().getColor(R.color.white20));
        blackPaint.setStrokeWidth(1.0f);

        Log.e(TAG, "WaveView--> 4");
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.e(TAG, "WaveView-->3 ");
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);


        Log.e(TAG, "WaveView-->2 mCacheBitMap:" + mCacheBitMap);
    }

    public WaveView(Context context) {
        super(context);
        Log.e(TAG, "WaveView--> 1");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mCacheBitMap, mShowRect, rect, null);
    }

    public void reflush(int vaule) {
        index++;
        if (index == cacheCount) {
            index = cacheCount / 2;
        }
        if (index >= maxLinPreWidth) {
            mShowRect.left++;
            mShowRect.right++;
        }
        drawView(vaule);
    }

    /**
     * 缓存画布
     */
    private void initCache() {
        int height = getHeight();
        int width = getWidth();

        mShowRect = new Rect(0, 0, width, height);
        rect = new Rect(0, 0, getWidth(), getHeight());

        //每屏可以画多少个
        maxLinPreWidth = width / perPix;

        if ((width % perPix) != 0) {
            maxLinPreWidth++;
        }

        cacheCount = maxLinPreWidth * 2;
        Log.e(TAG, "initCache--> cacheCount：" + cacheCount);

        //缓存画布可以画2屏
        mCacheBitMap = Bitmap.createBitmap(cacheCount * perPix, height, Bitmap.Config.ARGB_8888);
        mCacheCanvas = new Canvas(mCacheBitMap);


    }

    /**
     * 在缓存上画
     *
     * @param vaule
     */
    private void drawView(int vaule) {
        float x = index * perPix;
        float h = vaule * DensityUtil.dip2px(200) / 100;
        float y1 = (DensityUtil.dip2px(200) + DensityUtil.dip2px(20)) / 2 - h / 2;
        float y2 = y1 + h;
        mCacheCanvas.drawLine(x, y1, x, y2, whilePaint);
        Log.e(TAG, "drawView--> index:" + index);
        Log.e(TAG, "drawView--> cacheCount:" + cacheCount);

        //超过2屏的时，把第二屏重新画
        if (index + maxLinPreWidth >= cacheCount) {
            x = (index - cacheCount/2) * perPix;
            mCacheCanvas.drawLine(x, y1, x, y2, whilePaint);

            Log.e(TAG, "drawView--> 2222222222222:" + x);
        }/* else {
            mCacheCanvas.drawLine(x, y1, x, y2, whilePaint);

            Log.e(TAG, "drawView-->11111111111: " + x);
        }*/

        if (index >= maxLinPreWidth) {
            int right = (index) * perPix;
            mShowRect.set(right - getWidth(), 0, right
                    , getHeight());
        }

        invalidate();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initCache();

    }

}
