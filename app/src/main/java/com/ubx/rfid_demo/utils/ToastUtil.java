package com.ubx.rfid_demo.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    private static Context mContext ;
    /**
     * Toast实例，用于对本页出现的所有Toast进行处理
     */
    private static Toast myToast;

    private ToastUtil() {
        throw new RuntimeException("ToastUtils cannot be initialized!");
    }

    public static void init(Context context) {
        mContext = context;
    }

    /**
     * 此处是一个封装的Toast方法，可以取消掉上一次未完成的，直接进行下一次Toast
     * @param text 需要toast的内容
     */
    public static void toast( String text){
        if (myToast != null) {
            myToast.cancel();
            myToast=Toast.makeText(mContext,text,Toast.LENGTH_SHORT);
        }else{
            myToast=Toast.makeText(mContext,text,Toast.LENGTH_SHORT);
        }
        myToast.show();
    }

}
