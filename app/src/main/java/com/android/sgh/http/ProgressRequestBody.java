package com.android.sgh.http;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/***
 *Created by sgh
 *on 2019\6\24 0024
 *
 * 带上传进度的RequestBody
 *
 */
public class ProgressRequestBody extends RequestBody {
    private RequestBody requestBody;
    OkHttpManager.UiProgressUpdate mListener;
    private BufferedSink bufferedSink;
    private MyHandler myHandler;
    private int lastProgress;

    public ProgressRequestBody(RequestBody body, OkHttpManager.UiProgressUpdate listener) {
        requestBody = body;
        mListener = listener;
        if (myHandler == null) {
            myHandler = new MyHandler();
        }
    }

    class MyHandler extends Handler {
        private MyHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Bundle bundle = msg.getData();
                    int pro = bundle.getInt("progress");
                    if (mListener != null) {
                        mListener.updateProgress(pro);
                    }
                    break;
            }
        }
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));
        }
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    private Sink sink(BufferedSink sink) {
        return new ForwardingSink(sink) {
            long bytesWritten = 0L;
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                }
                bytesWritten += byteCount;
                int progress = (int) (bytesWritten * 100 / contentLength);
                if (progress != lastProgress) {
                    Message msg = Message.obtain();
                    msg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putInt("progress", progress);
                    msg.setData(bundle);
                    myHandler.sendMessage(msg);
                }
                lastProgress = progress;
            }
        };
    }
}
