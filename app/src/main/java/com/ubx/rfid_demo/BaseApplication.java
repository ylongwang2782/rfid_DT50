package com.ubx.rfid_demo;

import android.app.Application;
import android.content.Context;

import com.ubx.rfid_demo.utils.ToastUtil;

public class BaseApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        ToastUtil.init(mContext);
    }

    public static Context getContext() {
        return mContext;
    }





}
