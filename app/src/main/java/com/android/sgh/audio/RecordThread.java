package com.android.sgh.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 录音相关的线程
 */
public class RecordThread extends Thread {

    private static final String TAG = "RecordThread";

    String pcmPath;
    String wavPath;
    FileOutputStream fileOutputStream;

    //配置参数
    private static int sampleRateInHz = 16000;//采样率
    private static final int channelConfig = AudioFormat.CHANNEL_IN_MONO;//声道CHANNEL_OUT_STEREO、CHANNEL_OUT_MONO
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;//PCM编码
    private AudioRecord audioRecord;
    //标志位
    public static boolean isRecording = false;
    private int bufferSize;
    private boolean isPause = false;

    int i=1;

    /**
     * 录音需要传入码率字段
     */
    public RecordThread() {
        bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, bufferSize);
    }


    @Override
    public void run() {
        try {
            Log.e(TAG, "RecordThread run..." + bufferSize);
            byte[] buffer = new byte[bufferSize];
            audioRecord.startRecording();
            while (isRecording) {
//                Log.e(TAG, "RecordThread run..." +  buffer.length+"     "+i);
                i++;
                audioRecord.read(buffer, 0, buffer.length);
                if (!isPause) {
                    fileOutputStream.write(buffer);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始录音
     */
    public void startRecord(String wavpath) {
        this.wavPath = wavpath;
        createTempFile(wavpath);
        isRecording = true;
        start();
        Log.e(TAG, "开始录音");
    }

    public void pauseRecord() {
        isPause = true;
    }

    public void resumeRecord() {
        isPause = false;
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        try {
            Thread.sleep(50);
            isRecording = false;
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                i=1;
                Log.e(TAG, "停止录音");
            }
            fileOutputStream.close();
            PcmToWavUtil util = new PcmToWavUtil(sampleRateInHz, channelConfig, audioFormat);
            util.pcmToWav(pcmPath, wavPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void createTempFile(String wavpath) {

        try {
            File file = new File(wavpath);
            String fileName = file.getName();
            String[] str = fileName.split(".wav");
            pcmPath = file.getParent() + "/" + str[0] + ".pcm";
            File pcmFile = new File(pcmPath);
            if (!pcmFile.exists()) {
                pcmFile.createNewFile();
            }
            Log.e(TAG, "createTempFile-->pcmPath: " + pcmPath);
            Log.e(TAG, "createTempFile-->wavPath: " + wavPath);

            fileOutputStream = new FileOutputStream(pcmFile);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "createTempFile--> " + e.getLocalizedMessage());
        }
    }

}
