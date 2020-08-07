package com.android.sgh.audio;

import android.os.Handler;
import android.util.Log;


/***
 * 合成音频
 *
 */
public class InsertAudioThread extends Thread {

    Handler handler;
    String srcPath;
    String insertPath;
    String outPath;
    int src_insert_time;

    public InsertAudioThread(Handler handler, String srcPath, String insertPath, String outPath, int src_insert_time) {
        this.handler = handler;
        this.srcPath = srcPath;
        this.insertPath = insertPath;
        this.outPath = outPath;
        this.src_insert_time = src_insert_time;
    }

    @Override
    public void run() {
        super.run();
        Log.e("myLog", "插入线程启动");
        boolean isSuccess = AudioUtils.mergeAudioFiles2(srcPath, insertPath, outPath, src_insert_time);
        if (isSuccess) {
            Log.e("myLog", "插入成功");
            handler.sendEmptyMessage(2);
        } else {
            Log.e("myLog", "插入失败");
            handler.sendEmptyMessage(3);
        }
    }
}
