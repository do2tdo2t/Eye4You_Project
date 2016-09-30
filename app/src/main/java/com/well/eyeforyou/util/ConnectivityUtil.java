package com.well.eyeforyou.util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.LocationManager;
import android.net.*;

/**
 * Created by 박효정 on 2016-08-07.
 */
public class ConnectivityUtil {
    private  final static  String TAG = "ConnectivityUtil";

    public static boolean getBluetoothConnectivityState(Context context){
        BluetoothAdapter bleAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bleAdapter != null && bleAdapter.isEnabled()){
            return true;
        }else{
            return false;
        }

    }

    public static boolean getInternetConnectivityState(Context context){
        android.net.ConnectivityManager mConnectivityManager = (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State networkInfo =  mConnectivityManager.getNetworkInfo(mConnectivityManager.TYPE_MOBILE).getState();
        NetworkInfo.State wifiInfo = mConnectivityManager.getNetworkInfo(mConnectivityManager.TYPE_WIFI).getState();
        if(networkInfo!= NetworkInfo.State.CONNECTED && wifiInfo != NetworkInfo.State.CONNECTED ){
            return false;
        }
        return true;
    }

    public static boolean getGPSConnectivityState(Context context){
        LocationManager mLocationManager =  (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPSEnabled == true) {
            return true;
        }else {
            return false;
        }
    }
}
