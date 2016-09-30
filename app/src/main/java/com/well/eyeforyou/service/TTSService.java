package com.well.eyeforyou.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.well.eyeforyou.R;
import com.well.eyeforyou.component.Constances;
import com.well.eyeforyou.util.LogManager;

import java.util.Locale;

public class TTSService extends Service {
    private TextToSpeech mTTS;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogManager.d("TTSService","onStartCommand()");
        mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTTS.setLanguage(Locale.KOREAN);
                    mTTS.speak(getString(R.string.service_create), TextToSpeech.QUEUE_FLUSH, null);
                }else{
                    LogManager.v("TTSService","textToSpeech error");
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTTS.shutdown();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mTTSMessenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        LogManager.d("TTSService","onRebind()");
        super.onRebind(intent);
    }

    final Messenger mTTSMessenger = new Messenger(new TTSServiceHandler());
    class TTSServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constances.MSG_SAY:
                    Log.v("TTSService", "TTSService Handler say :" + msg.obj +"arg1 :"+ msg.arg1);
                    if(msg.arg1 == Constances.TTS_NEXT_SAY) {
                        say((String) msg.obj);
                    }else if(msg.arg1 == Constances.TTS_STOP_SAY){
                        bequiet();
                        say((String) msg.obj);
                    }
                    break;
                case Constances.MSG_BEQUIET :
                    Log.v("TTSService", "TTSService Handler be quiet :");
                    bequiet();
                    break;
            }
        }
    }

    public void say(String str) {
        if(mTTS!= null) {
            if(mTTS.isSpeaking()){
                mTTS.speak(str, TextToSpeech.QUEUE_ADD, null);
            }else {
                mTTS.speak(str, TextToSpeech.QUEUE_ADD, null);
            }
        }else{
            mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        mTTS.setLanguage(Locale.KOREAN);
                    }else{
                        LogManager.v("TTSService","textToSpeech error");
                    }
                }
            });
            Log.v("TTSService","mTTS null");
        }
    }

    public void bequiet() {
        if (mTTS.isSpeaking()) {
            mTTS.stop();
        }
    }
}
