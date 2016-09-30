package com.well.eyeforyou.timer;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.well.eyeforyou.component.Constances;
import com.well.eyeforyou.util.LogManager;

import java.util.Timer;

/**
 * Created by user on 2016-09-22.
 */
public class TTSTimer extends Timer{
    private Message msg = new Message();
    private final static String TAG ="TTSTimer";
    public void say(Messenger m, String str){
        if(m== null){
            return;
        }
        if(msg == null){
            msg = new Message();
        }
        msg.what = Constances.MSG_SAY;
        msg.obj = str;
        msg.arg1 = Constances.TTS_STOP_SAY;
        try {
            m.send(msg);
        }catch (RemoteException e){
            LogManager.d(TAG, "error : "+ e.toString());
        }
    }
}
