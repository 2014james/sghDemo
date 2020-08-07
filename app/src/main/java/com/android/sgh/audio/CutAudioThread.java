package com.android.sgh.audio;

import android.os.Handler;
import android.util.Log;



/***
 *Created by sgh
 *on 2020\4\23
 *剪切音频
 */
public class CutAudioThread extends Thread {

    Handler handler;
    String audioPath;
    String cutPath;
    int startFrame;
    int endFrame;
    int msg;
    int longTime;



    public CutAudioThread(Handler handler, int msg, String audioPath, String cutPath, int startFrame, int endFrame) {

        this.handler = handler;
        this.msg = msg;
        this.audioPath = audioPath;
        this.cutPath = cutPath;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
    }

    //音频切割界面调用
    public CutAudioThread(Handler handler, int msg, String audioPath, String cutPath, int startFrame, int endFrame, int longTime) {
        this.handler = handler;
        this.msg = msg;
        this.audioPath = audioPath;
        this.cutPath = cutPath;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
        this.longTime = longTime;
    }

//    public CutAudioThread(Handler handler, int msg, String audioPath, String cutPath, int startFrame) {
//        this.handler = handler;
//        this.msg = msg;
//        this.audioPath = audioPath;
//        this.cutPath = cutPath;
//        this.startFrame = startFrame;
//    }

    @Override
    public void run() {
        super.run();
        boolean isCutSuccess = AudioUtils.cutAudioFile(audioPath, cutPath, startFrame, endFrame, longTime);
        if (isCutSuccess) {
            handler.sendEmptyMessage(msg);
        } else {
            handler.sendEmptyMessage(3);
        }
    }
}
