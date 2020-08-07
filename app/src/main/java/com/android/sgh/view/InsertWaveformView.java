/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sgh.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import com.android.sgh.R;
import com.android.sgh.audio.SoundFile;

import java.util.List;


/**
 * WaveformView 这个根据你的音频进行处理成完整的波形
 * 如果文件很大可能会很慢哦
 *
 * 插入音频
 *
 */
public class InsertWaveformView extends View {
    String TAG = InsertWaveformView.class.getSimpleName();
    // Colors
    private int line_offset;
    private Paint mGridPaint;
    //    private Paint mSelectedLinePaint;
    private Paint mUnselectedLinePaint;
    private Paint playPaint;
    private Paint wavePaint;
    Paint paintLine;
    private int playFinish;

    private SoundFile mSoundFile;
    private int[] mLenByZoomLevel;
    private double[][] mValuesByZoomLevel;
    private double[] mZoomFactorByZoomLevel;
    private int[] mHeightsAtThisZoomLevel;
    private int mZoomLevel;
    private int mNumZoomLevels;
    private int mSampleRate;
    private int mSamplesPerFrame;
    private int mOffset;
    private int mSelectionStart;
    private int mSelectionEnd;
    private int mPlaybackPos;
    private float mDensity;
    private float mInitialScaleSpan;
    private boolean mInitialized;
    private int state = 0;
    private Bitmap markIcon;
    private int bitWidth;
    private int bitHeight;

    private int insertPos = -1;

    public int getPlayFinish() {
        return playFinish;
    }

    public void setPlayFinish(int playFinish) {
        this.playFinish = playFinish;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getLine_offset() {
        return line_offset;
    }

    public void setLine_offset(int line_offset) {
        this.line_offset = line_offset;
    }

    public InsertWaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setFocusable(false);

        //画播放进度线
        playPaint = new Paint();
        playPaint.setColor(getResources().getColor(R.color.colorAccent));
        playPaint.setAntiAlias(true);
        playPaint.setStrokeWidth(2);

//        mSelectedLinePaint = new Paint();
//        mSelectedLinePaint.setAntiAlias(false);
//        mSelectedLinePaint.setColor(
//                getResources().getColor(R.color.waveform_selected));
        mUnselectedLinePaint = new Paint();
        mUnselectedLinePaint.setAntiAlias(false);
        mUnselectedLinePaint.setColor(getResources().getColor(R.color.white));

        //画标记文字
        markTextPaint = new Paint();
        markTextPaint.setColor(getResources().getColor(R.color.white20));
        markTextPaint.setTextSize(24);
        //标记图标
        markIcon = ((BitmapDrawable) getResources().getDrawable(R.mipmap.edit_mark)).getBitmap();
        bitWidth = markIcon.getWidth();
        bitHeight = markIcon.getHeight();

        //画标记线
        bottomHalfPaint = new Paint();
        bottomHalfPaint.setStrokeWidth(1);
        bottomHalfPaint.setColor(getResources().getColor(R.color.white20));

        wavePaint = new Paint();
        wavePaint.setColor(getResources().getColor(R.color.white));
        wavePaint.setStrokeWidth(2);

        mSoundFile = null;
        mLenByZoomLevel = null;
        mValuesByZoomLevel = null;
        mHeightsAtThisZoomLevel = null;
        mOffset = 0;
        mPlaybackPos = -1;
        mSelectionStart = 0;
        mSelectionEnd = 0;
        mDensity = 1.0f;
        mInitialized = false;
    }


    public boolean hasSoundFile() {
        return mSoundFile != null;
    }

    public void setSoundFile(SoundFile soundFile) {
        mSoundFile = soundFile;
        mSampleRate = mSoundFile.getSampleRate();
        mSamplesPerFrame = mSoundFile.getSamplesPerFrame();
        computeDoublesForAllZoomLevels();
        mHeightsAtThisZoomLevel = null;
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public int getZoomLevel() {

        return mZoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        while (mZoomLevel > zoomLevel) {
            zoomIn();
        }
        while (mZoomLevel < zoomLevel) {
            zoomOut();
        }
    }

    public boolean canZoomIn() {
        return (mZoomLevel > 0);
    }


    public void zoomIn() {
        if (canZoomIn()) {
            mZoomLevel--;
            mSelectionStart *= 2;
            mSelectionEnd *= 2;
            mHeightsAtThisZoomLevel = null;
            int offsetCenter = mOffset + getMeasuredWidth() / 2;
            offsetCenter *= 2;
            mOffset = offsetCenter - getMeasuredWidth() / 2;
            if (mOffset < 0)
                mOffset = 0;
            invalidate();
        }
    }

    public boolean canZoomOut() {
        return (mZoomLevel < mNumZoomLevels - 1);
    }

    public void zoomOut() {
        if (canZoomOut()) {
            mZoomLevel++;
            mSelectionStart /= 2;
            mSelectionEnd /= 2;
            int offsetCenter = mOffset + getMeasuredWidth() / 2;
            offsetCenter /= 2;
            mOffset = offsetCenter - getMeasuredWidth() / 2;
            if (mOffset < 0)
                mOffset = 0;
            mHeightsAtThisZoomLevel = null;
            invalidate();
        }
    }

    /**
     * 获取所有像素
     *
     * @return
     */
    public int maxPos() {
        return mLenByZoomLevel[mZoomLevel];
    }

    /**
     * 根据秒获取当前帧
     *
     * @param seconds
     * @return
     */
    public int secondsToFrames(double seconds) {
        return (int) (1.0 * seconds * mSampleRate / mSamplesPerFrame + 0.5);
    }

    /**
     * 毫秒获取帧
     *
     * @param seconds
     * @return
     */
    public int millisecsToFrames(double seconds) {
        return (int) (1.0 * seconds / 1000 * mSampleRate / mSamplesPerFrame + 0.5);
    }


    /**
     * 秒获取像素
     *
     * @param seconds
     * @return
     */
    public int secondsToPixels(double seconds) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int) (z * seconds * mSampleRate / mSamplesPerFrame + 0.5);
    }

    /**
     * 像素获取秒
     *
     * @param pixels
     * @return
     */
    public double pixelsToSeconds(int pixels) {
        double z = mZoomFactorByZoomLevel[0];
        return (mSoundFile.getmNumFramesFloat() * 2 * (double) mSamplesPerFrame / (mSampleRate * z));
    }

    /**
     * 像素获取毫秒
     *
     * @param pixels
     * @return
     */
    public int pixelsToMillisecs(int pixels) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int) (pixels * (1000.0 * mSamplesPerFrame) /
                (mSampleRate * z) + 0.5);
    }

    /**
     * 毫秒获取像素
     *
     * @param msecs
     * @return
     */
    public int millisecsToPixels(int msecs) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int) ((msecs * 1.0 * mSampleRate * z) /
                (1000.0 * mSamplesPerFrame) + 0.5);
    }


    /**
     * 获取所有时间毫秒
     *
     * @return
     */
    public long pixelsToMillisecsTotal() {
        return (long) (mSoundFile.getmNumFramesFloat() * 1 * (1000.0 * mSamplesPerFrame) /
                (mSampleRate * 1) + 0.5);
    }

    public void setParameters(int start, int end, int offset) {
        mSelectionStart = start;
        mSelectionEnd = end;
        mOffset = offset;
    }

    public int getStart() {
        return mSelectionStart;
    }

    public int getEnd() {
        return mSelectionEnd;
    }

    public int getOffset() {
        return mOffset;
    }

    /**
     * 设置当前播放帧位置
     *
     * @param pos
     */
    public void setPlayback(int pos) {
        mPlaybackPos = pos;
    }


    public void recomputeHeights(float density) {
        mHeightsAtThisZoomLevel = null;
        mDensity = density;
        invalidate();
    }


    protected void drawWaveformLine(Canvas canvas,
                                    int x, int y0, int y1,
                                    Paint paint) {

        int pos = maxPos();
        Log.e(TAG, "drawWaveformLine--> pos:" + pos);
        float rat = ((float) getMeasuredWidth() / pos);
        canvas.drawLine((int) (x * rat), y0, (int) (x * rat) + 1, y1, paint);
    }

    private List<Integer> flags;
    private Paint markTextPaint;
    private Paint bottomHalfPaint;


    /**
     * 设置时间标记点
     */
    public void setFlag(List<Integer> flagPositions) {
        this.flags = flagPositions;
        invalidate();//重新绘制音频
    }


    /**
     * 清除当前标记点
     */
    public void clearFlag() {
        if (flags != null) {
            flags.clear();//清除所有标记点
        }
    }

    public void setInsertPos(int topback) {
        insertPos = topback;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int height = measuredHeight - line_offset;

        Paint centerLine = new Paint();
        centerLine.setColor(Color.rgb(255, 255, 255));
        canvas.drawLine(0, height * 0.5f + line_offset / 2, measuredWidth, height * 0.5f + line_offset / 2, centerLine);//中心线

        paintLine = new Paint();
        paintLine.setColor(getResources().getColor(R.color.white20));
        if (state == 1) {
            mSoundFile = null;
            state = 0;
            return;
        }

        //TODO标记点的绘制
//        if (flags != null && flags.size() > 0) {
//            for (int i = 0; i < flags.size(); i++) {
//                long pos = flags.get(i);
//                if (pos > 0) {
//                    pos = pos * measuredWidth / pixelsToMillisecsTotal();
////            		Rect destRect=new Rect((int)(pos-bitWidth/4), 0, (int)(pos-bitWidth/4)+bitWidth/2, bitHeight/2);
//                    Rect destRect = new Rect((int) (pos - bitWidth / 2), 0, (int) (pos + bitWidth / 2), bitHeight);
//                    canvas.drawBitmap(markIcon, null, destRect, null);
//                    String text = (i + 1) + "";
//                    float textWidth = markTextPaint.measureText(text);
//                    FontMetricsInt fontMetricsInt = markTextPaint.getFontMetricsInt();
//                    int fontHeight = fontMetricsInt.bottom - fontMetricsInt.top;
////        			canvas.drawText(text, (pos-textWidth/2), fontHeight-8, markTextPaint);
//                    canvas.drawText(text, (pos - textWidth / 2), fontHeight - 8, markTextPaint);
////        			canvas.drawLine(pos-bitWidth/16+2, bitHeight/2-2,pos-bitWidth/16+2, measuredWidth-bitHeight/2, bottomHalfPaint );
//                    canvas.drawLine(pos, bitHeight - 2, pos, measuredWidth - bitHeight / 2, bottomHalfPaint);
//                }
//
//            }
//
//        }
        if (mSoundFile == null) {
            height = measuredHeight - line_offset;
            centerLine = new Paint();
            centerLine.setColor(getResources().getColor(R.color.white));
            canvas.drawLine(0, height * 0.5f + line_offset / 2, measuredWidth, height * 0.5f + line_offset / 2, centerLine);//中心线
            paintLine = new Paint();
            paintLine.setColor(getResources().getColor(R.color.white20));
            return;
        }
        if (mHeightsAtThisZoomLevel == null) {
            computeIntsForThisZoomLevel();
        }

        int start = mOffset;
        int width = mHeightsAtThisZoomLevel.length - start;
        int ctr = measuredHeight / 2;
        if (width > measuredWidth)
            width = measuredWidth;

        double onePixelInSecs = pixelsToSeconds(1);
        double fractionalSecs = mOffset * onePixelInSecs;
        int integerSecs = (int) fractionalSecs;
        int i = 0;
        while (i < width) {
            i++;
            fractionalSecs += onePixelInSecs;
            int integerSecsNew = (int) fractionalSecs;
            if (integerSecsNew != integerSecs) {
                integerSecs = integerSecsNew;
            }
        }
        boolean isbottom = false;
        //画选择区域
//        for (i = 0; i < maxPos(); i++) {
//            if (i + start == insertPos) {
//                mUnselectedLinePaint.setColor(MyApplication.getInstance().getColor(R.color.white));
//                canvas.drawLine(i * getMeasuredWidth() / maxPos(), 0, getMeasuredWidth(), getMeasuredHeight(), mUnselectedLinePaint);//垂直的线
//            }
//
//        }
        //滑动到结束 最后一帧
//        if (mPlaybackBottom != -1 && !isbottom) {
//            canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mUnselectedLinePaint);//垂直的线
//        }

        // 画声纹
        for (i = 0; i < maxPos(); i++) {

            //画音频
            drawWaveformLine(
                    canvas, i,
                    (ctr - mHeightsAtThisZoomLevel[start + i]),
                    (ctr + 1 + mHeightsAtThisZoomLevel[start + i]),
                    wavePaint);
            //画选择线
            if (i + start == insertPos) {
                canvas.drawLine(i * getMeasuredWidth() / maxPos(), 0, i * getMeasuredWidth() / maxPos(), getMeasuredHeight(), mUnselectedLinePaint);//垂直的线
            }

            //画正在播放的线
            if (i + start == mPlaybackPos && playFinish != 1) {
                canvas.drawLine(i * getMeasuredWidth() / maxPos(), 0, i * getMeasuredWidth() / maxPos(), measuredHeight, playPaint);
            }
        }

    }


    /**
     * Called once when a new sound file is added
     */
    private void computeDoublesForAllZoomLevels() {
        int numFrames = mSoundFile.getNumFrames();
        int[] frameGains = mSoundFile.getFrameGains();
        double[] smoothedGains = new double[numFrames];
        if (numFrames == 1) {
            smoothedGains[0] = frameGains[0];
        } else if (numFrames == 2) {
            smoothedGains[0] = frameGains[0];
            smoothedGains[1] = frameGains[1];
        } else if (numFrames > 2) {
            smoothedGains[0] = (double) (
                    (frameGains[0] / 2.0) +
                            (frameGains[1] / 2.0));
            for (int i = 1; i < numFrames - 1; i++) {
                smoothedGains[i] = (double) (
                        (frameGains[i - 1] / 3.0) +
                                (frameGains[i] / 3.0) +
                                (frameGains[i + 1] / 3.0));
            }
            smoothedGains[numFrames - 1] = (double) (
                    (frameGains[numFrames - 2] / 2.0) +
                            (frameGains[numFrames - 1] / 2.0));
        }

        // Make sure the range is no more than 0 - 255
        double maxGain = 1.0;
        for (int i = 0; i < numFrames; i++) {
            if (smoothedGains[i] > maxGain) {
                maxGain = smoothedGains[i];
            }
        }
        double scaleFactor = 1.0;
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain;
        }

        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0;
        int gainHist[] = new int[256];
        for (int i = 0; i < numFrames; i++) {
            int smoothedGain = (int) (smoothedGains[i] * scaleFactor);
            if (smoothedGain < 0)
                smoothedGain = 0;
            if (smoothedGain > 255)
                smoothedGain = 255;
            if (smoothedGain > maxGain)
                maxGain = smoothedGain;

            gainHist[smoothedGain]++;
        }

        // Re-calibrate the min to be 5%
        double minGain = 0;
        int sum = 0;
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[(int) minGain];
            minGain++;
        }

        // Re-calibrate the max to be 99%
        sum = 0;
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[(int) maxGain];
            maxGain--;
        }
        if (maxGain <= 50) {
            maxGain = 80;
        } else if (maxGain > 50 && maxGain < 120) {
            maxGain = 142;
        } else {
            maxGain += 10;
        }


        // Compute the heights
        double[] heights = new double[numFrames];
        double range = maxGain - minGain;
        for (int i = 0; i < numFrames; i++) {
            double value = (smoothedGains[i] * scaleFactor - minGain) / range;
            if (value < 0.0)
                value = 0.0;
            if (value > 1.0)
                value = 1.0;
            heights[i] = value * value;
        }

        mNumZoomLevels = 5;
        mLenByZoomLevel = new int[5];
        mZoomFactorByZoomLevel = new double[5];
        mValuesByZoomLevel = new double[5][];

        // Level 0 is doubled, with interpolated values
        mLenByZoomLevel[0] = numFrames * 2;
        System.out.println("ssnum" + numFrames);
        mZoomFactorByZoomLevel[0] = 2.0;
        mValuesByZoomLevel[0] = new double[mLenByZoomLevel[0]];
        if (numFrames > 0) {
            mValuesByZoomLevel[0][0] = 0.5 * heights[0];
            mValuesByZoomLevel[0][1] = heights[0];
        }
        for (int i = 1; i < numFrames; i++) {
            mValuesByZoomLevel[0][2 * i] = 0.5 * (heights[i - 1] + heights[i]);
            mValuesByZoomLevel[0][2 * i + 1] = heights[i];
        }

        // Level 1 is normal
        mLenByZoomLevel[1] = numFrames;
        mValuesByZoomLevel[1] = new double[mLenByZoomLevel[1]];
        mZoomFactorByZoomLevel[1] = 1.0;
        for (int i = 0; i < mLenByZoomLevel[1]; i++) {
            mValuesByZoomLevel[1][i] = heights[i];
        }

        // 3 more levels are each halved
        for (int j = 2; j < 5; j++) {
            mLenByZoomLevel[j] = mLenByZoomLevel[j - 1] / 2;
            mValuesByZoomLevel[j] = new double[mLenByZoomLevel[j]];
            mZoomFactorByZoomLevel[j] = mZoomFactorByZoomLevel[j - 1] / 2.0;
            for (int i = 0; i < mLenByZoomLevel[j]; i++) {
                mValuesByZoomLevel[j][i] =
                        0.5 * (mValuesByZoomLevel[j - 1][2 * i] +
                                mValuesByZoomLevel[j - 1][2 * i + 1]);
            }
        }


        if (numFrames > 5000) {
            mZoomLevel = 3;
        } else if (numFrames > 1000) {
            mZoomLevel = 2;
        } else if (numFrames > 300) {
            mZoomLevel = 1;
        } else {
            mZoomLevel = 0;
        }

        mInitialized = true;
    }

    /**
     * Called the first time we need to draw when the zoom level has changed
     * or the screen is resized
     */
    private void computeIntsForThisZoomLevel() {

        int halfHeight = (getMeasuredHeight() / 2) - 1;
        mHeightsAtThisZoomLevel = new int[mLenByZoomLevel[mZoomLevel]];
        for (int i = 0; i < mLenByZoomLevel[mZoomLevel]; i++) {
            mHeightsAtThisZoomLevel[i] =
                    (int) (mValuesByZoomLevel[mZoomLevel][i] * halfHeight);
        }
    }
}
