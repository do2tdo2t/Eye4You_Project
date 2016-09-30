package com.well.eyeforyou.util;

import android.app.Activity;
import android.util.Log;

/**
 * Created by 박효정 on 2016-08-02.
 */
public class LogManager {
    //public static boolean D = false;
    public static boolean D = true;

    public static void d(String tag, String msg){
        if(D && tag != null && msg != null)
            Log.d(tag, msg);
    }

    public static void d (Activity activity , String msg){
        if(D && activity != null && msg != null)
            Log.d(activity.getLocalClassName(), msg);
    }

    public static void i(String tag, String msg){
        if(D && tag != null && msg != null)
            Log.i(tag, msg);
    }

    public static void i (Activity activity , String msg){
        if(D && activity != null && msg != null)
            Log.i(activity.getLocalClassName(), msg);
    }

    public static void e(String tag, String msg){
        if(D && tag != null && msg != null)
            Log.e(tag, msg);
    }

    public static void e (Activity activity , String msg){
        if(D && activity != null && msg != null)
            Log.e(activity.getLocalClassName(), msg);
    }

    public static void v(String tag, String msg){
        if(D && tag != null && msg != null)
            Log.v(tag, msg);
    }

    public static void v (Activity activity , String msg){
        if(D && activity != null && msg != null)
            Log.v(activity.getLocalClassName(), msg);
    }

}
