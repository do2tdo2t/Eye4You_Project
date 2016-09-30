package com.well.eyeforyou.timer;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;

import android.os.Message;

import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.well.eyeforyou.component.Constances;
import com.well.eyeforyou.util.LogManager;
import com.well.eyeforyou.vo.PoisCategoryMap;

import java.util.ArrayList;


/**
 * Created by 박효정 on 2016-09-05.
 */
public class SSTTimer extends CountDownTimer {
    private static final String TAG ="SSTTimer";
    private Context context;
    private SpeechRecognizer mRecognizer;
    private String sstResult ;
    private int error;
    private Handler mainHandler;
    Messenger messenger;

    public SSTTimer(long millisInFuture, long countDownInterval, Context context, Handler mainHandler, Messenger mTTSMessenger) {
       super(millisInFuture, countDownInterval);
        messenger = mTTSMessenger;
        this.context = context;
        this.mainHandler = mainHandler;
        startListening();
    }

    @Override
    public void onTick(long l) {

    }

    @Override
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

    public void startListening(){

        Intent sstIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sstIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        sstIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        sstResult = null;

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        mRecognizer.setRecognitionListener(sstListener);
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

    public void sendResultToMainActivity() {

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

    public RecognitionListener sstListener = new RecognitionListener() {
        ArrayList<String> mResult;
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            LogManager.d(TAG, "onReadyForSpeech................");
        }
        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
            LogManager.d(TAG, "EndOfSpeech................");
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
            String  key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;

            ArrayList<String> mResult = bundle.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            LogManager.d(TAG,"rs array : "+ mResult);
            if(rs[0]!=null && rs[0]!="") {
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
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }
    };
//
//
//    public void sendMessageToTTS(String str) {
//        Message msg = new Message();
//        if (messenger != null) {
//            msg.what = Constances.MSG_SAY;
//            msg.obj = str;
//            try {
//                messenger.send(msg);
//            } catch (RemoteException e) {
//                LogManager.d(TAG, "error : " + e.toString());
//            }
//        } else {
//            LogManager.v(TAG, "mTTSMessenger is null");
//        }
//    }


}
