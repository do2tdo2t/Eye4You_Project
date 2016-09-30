package com.well.eyeforyou.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.well.eyeforyou.component.Constances;
import com.well.eyeforyou.util.LogManager;
import com.well.eyeforyou.vo.PoisCategoryMap;

import java.util.ArrayList;

public class SSTService extends Service implements RecognitionListener {
    private final static String TAG ="SSTService";
    private SpeechRecognizer mRecognizer;
    private String sstResult ;
    private int error;
    private static Handler mainHandler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder LocalBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public SSTService getService(Handler mainHandler) {
            SSTService.mainHandler = mainHandler;
            return SSTService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return LocalBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startListening(){
        Intent sstIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sstIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        sstIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        sstResult = null;
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(SSTService.this);
        mRecognizer.setRecognitionListener(this);
        mRecognizer.startListening(sstIntent);

        error =0;
    }

    public void stopListening(){
        if(mRecognizer!= null){
            mRecognizer.stopListening();
            mRecognizer.destroy();
            mRecognizer= null;
        }
    }

    public void onFinish() {
        stopListening();
        LogManager.d(TAG,"finish timer");
        if(error == 0){
            if(sstResult == null || sstResult == ""|| sstResult.equals("null")) {
                Message msg = mainHandler.obtainMessage();
                msg.what = 300;
                msg.arg1 = 6;
                mainHandler.sendMessage(msg);
            }else {
                String category = checkEqualCategory(sstResult);
                if (category != "") {
                    try {
                        LogManager.d(TAG,"category ok "+sstResult);
                        Message msg = mainHandler.obtainMessage();
                        //category가 설정 되었을 경우
                        msg.what = 200;
                        msg.obj = category;
                        mainHandler.sendMessage(msg);
                    } catch (Exception e) {
                        LogManager.e(TAG, "error : " + e.toString());
                    }
                } else if (category == "") {
                    //일치하는 결과가 없다.
                    Message msg = mainHandler.obtainMessage();
                    msg.what = 201;
                    msg.obj = sstResult;
                    mainHandler.sendMessage(msg);
                }
            }
        }else{
            Message msg = mainHandler.obtainMessage();
            msg.what = 300;
            msg.arg1 = error;
            mainHandler.sendMessage(msg);
        }
        error = 0;
    }


    private String checkEqualCategory(String sstResult) {
        //category에 맞게 보냈는지 아닌지를 먼저 검사하도록 설정하였는지 아닌지를 먼저
        PoisCategoryMap poisCategory = new PoisCategoryMap();
        int i = 0;
        sstResult = sstResult.trim();
        if(sstResult.contains(" ")){
            String[] sstResultArray = sstResult.split(" ");
            i = sstResultArray.length;
            for(int j = 0  ; j < i ; j++ ){
                if(poisCategory.map.containsKey(sstResultArray[j])){
                    String word = poisCategory.map.get(sstResultArray[j]);
                    Log.d(TAG, "word (get array) : " + word);
                    return word;
                }
            }
            return "";
        }else {
            if (poisCategory.map.containsKey(sstResult)) {
                String word = poisCategory.map.get(sstResult);
                Log.d(TAG, "word : " + word);
                return word;
            } else {
                Log.d(TAG, "sstResult : " + sstResult);
                return "";
            }
        }
    }


    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }


    @Override
    public void onError(int i) {
        error = i;
        switch (i){
            case 1:
                LogManager.e(TAG,"error : network timeout");
                break;
            case 2:
                LogManager.e(TAG,"error : network error");
                break;
            case 3:
                LogManager.e(TAG,"error : recording error");
                break;
            case 4:
                LogManager.e(TAG,"error : server error");
                break;
            case 5:
                LogManager.e(TAG,"error : client error");
                break;
            case 6:
                LogManager.e(TAG,"error : no voice");
                break;
            case 7:
                LogManager.e(TAG,"error : result incorrect.");
                break;
            case 8:
                LogManager.e(TAG,"error : The service already started.");
                break;
            case 9:
                LogManager.e(TAG,"error : The permission error");
                break;
        }
        onFinish();
    }


    @Override
    public void onResults(Bundle bundle) {
        LogManager.d(TAG, "onResult................");
        String key = "";
        key = SpeechRecognizer.RESULTS_RECOGNITION;

        ArrayList<String> mResult = bundle.getStringArrayList(key);
        String[] rs = new String[mResult.size()];
        mResult.toArray(rs);
        LogManager.d(TAG, "rs array : " + mResult);
        if (rs[0] != null && rs[0] != "") {
            sstResult = "";
            sstResult += rs[0];
            if (sstResult.contains(" ")) {
                String[] array = sstResult.split(" ");
                sstResult = array[1];
            }
            //sendMessageToTTS(sstResult + ".로 검색을 시작합니다.");
            onFinish();
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    class TTSServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    LogManager.v(TAG, "TTSService Handler say :" + msg.obj);
                    startListening();
                    break;
                case 300 :
                    LogManager.v("TTSService", "TTSService Handler be quiet :");

                    break;
            }
        }
    }

}
