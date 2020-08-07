package com.android.sgh.http;


import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/***
 * 使用OkHttp框架执行网络请求
 * 支持 get、post、文件上传、文件下载
 *
 */
public class OkHttpManager {
    private static final String TAG = "OkHttpManager";
    public static String TYPE_FILE = "multipart/form-data";
    //提交json数据
    private static final MediaType JSON = MediaType.parse(
            "application/json;charset=utf-8");
    //提交字符串数据
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse(
            "text/x-markdown;charset=utf-8");
    public final static int CONNECT_TIMEOUT = 60;
    public final static int READ_TIMEOUT = 100;
    public final static int WRITE_TIMEOUT = 60;
    private static OkHttpManager mInstance;
    private OkHttpClient mOkHttpClient;

    private OkHttpManager() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    public synchronized static OkHttpManager getInstance() {
        if (mInstance == null) {
            mInstance = new OkHttpManager();
        }
        return mInstance;
    }

    /**
     * get请求
     *
     * @param url
     * @param callback
     */
    public void getRequest(String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * post请求，传集合参数
     *
     * @param url
     * @param callback
     * @param params
     */
    public void postRequest(String url, List<OkHttpParam> params, Callback callback) {
        Request request = buildPostRequest(url, params);
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    /***
     * post请求，传json参数
     * @param url
     * @param callback
     * @param json
     */
    public void postRequest(String url, String json, Callback callback) {
        Request request = buildPostRequest(url, json);
        mOkHttpClient.newCall(request).enqueue(callback);
    }


    /**
     * 上传文件
     *
     * @param url
     * @param file
     * @param callback
     */
    public void postFile(String url, File file, Callback callback) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        MultipartBody requestBody = new MultipartBody.Builder().addFormDataPart("filename", file.getName(), fileBody).build();
        Request requestPostFile = new Request.Builder()
                .url(url)
                .post(requestBody).addHeader("Content-Type", "application/json")
                .build();
        mOkHttpClient.newCall(requestPostFile).enqueue(callback);
    }

    /**
     * 带上传进度的
     *
     * @param url
     * @param file
     * @param callback
     * @param uiProgressUpdate
     */
    public void postFile(String url, File file, Callback callback, UiProgressUpdate uiProgressUpdate) {
        RequestBody fileBody = RequestBody.create(MediaType.parse(TYPE_FILE), file);
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file", file.getName(), fileBody).build();
        Request requestPostFile = new Request.Builder()
                .url(url)
                .post(new ProgressRequestBody(requestBody, uiProgressUpdate))
                .build();
        mOkHttpClient.newCall(requestPostFile).enqueue(callback);
    }

    /**
     * 下载文件
     */
    public void downLoadFile(String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }


    /**
     * 多个参数请求
     *
     * @param url
     * @param params
     * @return
     */
    private Request buildPostRequest(String url, List<OkHttpParam> params) {
        FormBody.Builder builder = new FormBody.Builder();
        for (OkHttpParam param : params) {
            builder.add(param.key, param.value);
        }
        RequestBody formBody = builder.build();
        return new Request.Builder().url(url).post(formBody).build();
    }

    /**
     * json参数
     *
     * @param url
     * @param json
     * @return
     */
    private Request buildPostRequest(String url, String json) {
        RequestBody requestBody = RequestBody.create(JSON, json);
        return new Request.Builder().url(url).post(requestBody).build();
    }


    /**
     * post请求参数类
     */
    public static class OkHttpParam {

        String key;
        String value;

        public OkHttpParam() {
        }

        public OkHttpParam(String key, String value) {
            this.key = key;
            this.value = value;
        }

    }

    public static interface UiProgressUpdate {
        void updateProgress(int progress);
    }

}
