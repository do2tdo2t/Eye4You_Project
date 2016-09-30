package com.well.eyeforyou.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.well.eyeforyou.util.ConnectivityUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private Context context;
    public NetworkChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        boolean status = ConnectivityUtil.getInternetConnectivityState(context);
        if(status==false){
            Toast.makeText(context,"인터넷 불안정", Toast.LENGTH_LONG).show();
        }
    }
}
