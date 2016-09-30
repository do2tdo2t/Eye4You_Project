package com.well.eyeforyou.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.well.eyeforyou.R;
import com.well.eyeforyou.util.ConnectivityUtil;

public class BluetoothChangeReceiver extends BroadcastReceiver {
    public BluetoothChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.;
        boolean status = ConnectivityUtil.getBluetoothConnectivityState(context);
        if(status==false){
            Toast.makeText(context, context.getString(R.string.ble_off), Toast.LENGTH_SHORT).show();
        }
    }
}
