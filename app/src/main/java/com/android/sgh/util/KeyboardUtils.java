package com.android.sgh.util;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/***
 *Created by sgh
 *2019\5\23
 *
 * 键盘处理工具类
 *
 */
public class KeyboardUtils {

    private static Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 弹出软键盘
     * EditText获取焦点时调用
     *
     * @param editText
     */
    public static void showSoftInput(EditText editText) {
        InputMethodManager inputManager =
                (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(editText, 0);
    }

    /**
     * 对话框延时弹出软键盘
     * 要在对话框弹出之后，延时一段时间在弹出软键盘
     * 在对话框show之后调用
     *
     * @param editText
     */
    public static void showSoftInputDeylayed(final EditText editText) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputManager =
                        (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }
        }, 200);
    }

    /**
     * 隐藏软键盘
     * EditText失去焦点时调用
     *
     * @param editText
     */
    public static void hideSoftInput(EditText editText) {
        InputMethodManager inputManager =
                (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

}
