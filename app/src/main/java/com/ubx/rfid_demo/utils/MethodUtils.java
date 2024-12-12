package com.ubx.rfid_demo.utils;

import android.util.Log;

import com.ubx.usdk.rfid.RfidManager;

public class MethodUtils {

    private static final String TAG = MethodUtils.class.getSimpleName();

    /**
     *
     * @return true：attached   false：detached
     */
    public boolean getRfidReaderStatus(RfidManager mRfidManager){
        try {
            if ( mRfidManager != null){
                boolean isLive=  mRfidManager.isLive();
                Log.e(TAG,"getFrequencyRegion  isLive:"+isLive);
                return isLive;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     *
     * @return true：attached   false：detached
     */
    public String GetDeviceID(RfidManager mRfidManager){
        try {
            if ( mRfidManager != null){
                String sn=  mRfidManager.getDeviceId();
                Log.e(TAG,"GetDeviceID  sn:"+sn);
                return sn;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

}
