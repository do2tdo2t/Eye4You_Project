package com.well.eyeforyou.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.well.eyeforyou.R;
import com.well.eyeforyou.component.Constances;
import com.well.eyeforyou.util.LogManager;
import com.well.eyeforyou.parser.PoisJsonParser;
import com.well.eyeforyou.vo.Poi;
import com.well.eyeforyou.vo.Pois;

import org.json.JSONObject;

public class POIService extends Service {
    //String strUrl = Constances.TmapURL;
    private final static String TAG = "POIService";
    private boolean isSetCategory;
    public static Handler mainHandler;
    AQuery aq = null;
    private boolean isBoundedTTSService = false;

    public IBinder LocalBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public POIService getService(Handler mainHandler) {
            POIService.mainHandler = mainHandler;
            return POIService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isBoundedTTSService){
            Intent i = new Intent(this,TTSService.class);
            bindService(i, mTTSConnection, Context.BIND_AUTO_CREATE);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogManager.d(TAG,"onbind()");
        if(!isBoundedTTSService&& mTTSMessenger == null){
            Intent i = new Intent(this,TTSService.class);
            bindService(i, mTTSConnection, Context.BIND_AUTO_CREATE);
        }
        // TODO: Return the communication channel to the service.
        return LocalBinder;
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
    }


    public void startPOIAsynchTask(String query, boolean isSetCategory){
        LogManager.d(TAG, "startPOIAsynchTask()");
        this.isSetCategory = isSetCategory;
        POIAsynchTask task = new POIAsynchTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,query);
    }

    public class POIAsynchTask extends AsyncTask<String, Void, Pois> {
        boolean isError = false;
        String searchKeyword = "&searchKeyword=name";
        String searchType = "&searchType=all";
        Pois pois = null;

        @Override
        protected Pois doInBackground(String... params) {
            String query = (String) params[0];
            String strUrl = Constances.TmapURL;
            strUrl += query;
            LogManager.d("POIService", "url :" + strUrl);

            AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
            final AQuery aq = new AQuery(POIService.this);
            cb.url(strUrl).type(JSONObject.class).weakHandler(this, "JsonCallback");
            cb.header("appKey", Constances.AppKey);
            cb.header("Content-Type", "application/json; charset=utf-8");

            aq.sync(cb);
            AjaxStatus status = cb.getStatus();
            int code = status.getCode();

            if (code == Constances.MSG_OK) {
                JSONObject object = cb.getResult();
                LogManager.d("POIService", "MSG_OK");
                pois = new Pois();
                pois = PoisJsonParser.parse(object);
                return pois;
            } else {
                isError = true;
                switch (code) {
                    case 101:
                        //network error
                        LogManager.e("POIService", "network error");
                        return null;
                    case 400:
                        LogManager.e("POIService", "status : " + status.getError());
                        //필수 파라미터가 없습니다.
                }
                return null;
            }
        }

        @Override
        protected void onPostExecute(Pois pois) {
            LogManager.d("POIService", "onPostExecute()");
            super.onPostExecute(pois);
            if (isError == true) {
                sendMessageToTTS(getString(R.string.poi_error));
                isError = false;
            } else {
                if (Integer.valueOf(pois.totalCount) == 0) {
                    sendMessageToTTS(getString(R.string.poi_no_result));
                } else {
                    sendMessageMainActivity(pois);
                }
            }
        }
    }

    private void sendMessageMainActivity(Pois pois) {
        LogManager.d(TAG, "sendMessageMainActivity()");
        Message msg = mainHandler.obtainMessage();
        if (isSetCategory) {
            LogManager.d(TAG,"POI_MSG_OK1");
            msg.what = Constances.POI_MSG_OK1;
        } else {
            LogManager.d(TAG,"POI_MSG_OK2");
            msg.what = Constances.POI_MSG_OK2;
        }
        msg.obj = pois;
        mainHandler.sendMessage(msg);
    }

    @Override
    public void onDestroy() {
        if(isBoundedTTSService){
            unbindService(mTTSConnection);
            isBoundedTTSService = false;
        }
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogManager.d(TAG,"onUnbind()");
        return super.onUnbind(intent);
    }

    private Messenger mTTSMessenger;
    private ServiceConnection mTTSConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mTTSMessenger = new Messenger(service);
            isBoundedTTSService = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mTTSMessenger = null;
            isBoundedTTSService = false;
        }
    };

    private void sendMessageToTTS(String str){
        Message mMsg = new Message();
        mMsg.what = Constances.MSG_SAY;
        mMsg.obj = str;
        try {
            mTTSMessenger.send(mMsg);
        }catch (RemoteException e){
            LogManager.d(TAG, "error : "+ e.toString());
        }
    }
}
