package com.android.sgh.audio;

import android.os.Handler;
import android.os.Message;

import com.max.demo.MixManager;

/**
 * 重采样线程
 */
public class ResampleThread extends Thread {
    private String path;
    private String outPath;
    private String name;
    private Handler mHandler;
    private int sample_rate = 16000;
    private MixManager manager;

    public ResampleThread(String path, String outPath, String name, Handler handler) {
        this.path = path;
        this.outPath = outPath;
        this.name = name;
        mHandler = handler;
        manager = new MixManager();
    }

    @Override
    public void run() {
        int i = manager.TransMp3ToWav(path, outPath, sample_rate, 1, 255);
        if (i == 0) {
            Message msg = Message.obtain();
            msg.what = 1;
            msg.obj = outPath;
            mHandler.sendMessage(msg);
        }
    }

}


