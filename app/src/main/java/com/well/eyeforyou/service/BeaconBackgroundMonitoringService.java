package com.well.eyeforyou.service;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOBeaconRegionState;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECOMonitoringListener;
import com.perples.recosdk.RECORangingListener;
import com.perples.recosdk.RECOServiceConnectListener;
import com.well.eyeforyou.component.Constances;
import com.well.eyeforyou.parser.BeaconJsonParser;
import com.well.eyeforyou.util.LogManager;
import com.well.eyeforyou.vo.Beacon;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

public class BeaconBackgroundMonitoringService extends Service implements RECOMonitoringListener, RECOServiceConnectListener {
    private final static String TAG = "BackgroundMonitoringService";
    /**
     * We recommend 1 second for scanning, 10 seconds interval between scanning, and 60 seconds for region expiration time.
     * 1초 스캔, 10초 간격으로 스캔, 60초의 region expiration time은 당사 권장사항입니다.
     */

    private RECOBeaconManager mRecoManager;
    private ArrayList<RECOBeaconRegion> mRegions;
    private static ArrayList<String> mBeaconsUuidList;
    public boolean isError = false;
    public boolean isStart = false;

    //private static ArrayList<String> rangingIdList;

    @Override
    public IBinder onBind(Intent intent) {
        LogManager.d(TAG, "onbind()");
        mBeaconsUuidList = new ArrayList<>();
        mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), Constances.SCAN_RECO_ONLY, Constances.ENABLE_BACKGROUND_RANGING_TIMEOUT);
        this.bindRECOService();
        return LocalBinder;
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        LogManager.d(TAG, "onUnBind()");
        stopMonitoring();
        mBeaconsUuidList.clear();
        super.unbindService(conn);
    }

    public boolean onUnbind(Intent intent) {
        LogManager.d(TAG, "onUnbind()");
        return super.onUnbind(intent);
    }

    private final IBinder LocalBinder = new LocalBinder();
    private static Handler mainHandler;

    public class LocalBinder extends Binder {
        public BeaconBackgroundMonitoringService getService(Handler handler) {
            BeaconBackgroundMonitoringService.mainHandler = handler;
            return BeaconBackgroundMonitoringService.this;
        }
    }

    public void onCreate() {
        Log.i("BackMonitoringService", "onCreate()");

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogManager.d(TAG, "onStartCommand()");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        this.tearDown();
        super.onDestroy();
    }

    public void onTaskRemoved(Intent rootIntent) {
        Log.i("BackMonitoringService", "onTaskRemoved()");
        super.onTaskRemoved(rootIntent);
    }

    private void bindRECOService() {
        Log.i("BackMonitoringService", "bindRECOService()");

        mRegions = new ArrayList<RECOBeaconRegion>();
        this.generateBeaconRegion();
        mRecoManager.setMonitoringListener(this);

        mRecoManager.bind(this);
    }

    private void generateBeaconRegion() {
        LogManager.d(TAG, "generateBeaconRegion()");
        RECOBeaconRegion recoRegion;
        recoRegion = new RECOBeaconRegion(Constances.RECO_UUID1, "Eye4U");
        recoRegion.setRegionExpirationTimeMillis(Constances.mRegionExpirationTime);
        mRegions.add(recoRegion);
    }

    @Override
    public void onServiceConnect() {
        LogManager.d(TAG, "onServiceConnect()");
        this.startMonitoring();
    }

    @Override
    public void onServiceFail(RECOErrorCode recoErrorCode) {
        LogManager.d(TAG, "onServiceFail()");
    }


    public void startMonitoring() {
        LogManager.i(TAG, "startMonitoring()");

        mRecoManager.setScanPeriod(Constances.mScanDuration);
        mRecoManager.setSleepPeriod(Constances.mSleepDuration);

        for (RECOBeaconRegion region : mRegions) {
            try {
                mRecoManager.startMonitoringForRegion(region);
                //rangingIdList.add(region.getProximityUuid());
                //new AsynchTask().execute(region.getProximityUuid());
                LogManager.d(TAG, region.getProximityUuid() + "  " + region.getUniqueIdentifier());
            } catch (RemoteException e) {
                LogManager.d(TAG, "RemoteException has occured while executing RECOManager.startMonitoringForRegion()");
                e.printStackTrace();
            } catch (NullPointerException e) {
                LogManager.d(TAG, "NullPointerException has occured while executing RECOManager.startMonitoringForRegion()");
                e.printStackTrace();
            }
        }
    }

    private void stopMonitoring() {
        Log.i("BackRangingService", "stopMonitoring()");

        for (RECOBeaconRegion region : mRegions) {
            try {
                mRecoManager.stopMonitoringForRegion(region);
            } catch (RemoteException e) {
                Log.e("BackRangingService", "RemoteException has occured while executing RECOManager.stopMonitoringForRegion()");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.e("BackRangingService", "NullPointerException has occured while executing RECOManager.stopMonitoringForRegion()");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void didEnterRegion(RECOBeaconRegion region, Collection<RECOBeacon> beacons) {
        LogManager.d(TAG, "didEnterRegion()");
    }

    @Override
    public void didExitRegion(RECOBeaconRegion region) {
        LogManager.d(TAG, "didExitRegion()");
        sendStopMessageToMain();
    }

    @Override
    public void didStartMonitoringForRegion(RECOBeaconRegion recoBeaconRegion) {
        LogManager.d(TAG, "didStartMonitoringForRegion()");
        //parsing
        if (recoBeaconRegion.getProximityUuid().equals(Constances.RECO_UUID1)) {
            LogManager.d(TAG, "task.execute");

            AsynchTask task = new AsynchTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,recoBeaconRegion.getProximityUuid());

            //beacon 만들어서 넣기 !
//
//            LogManager.d(TAG, "didStartMonitoringForRegion() - " + recoBeaconRegion.getProximityUuid());
//            Beacon beacon = new Beacon();
//            beacon.uuid = Constances.RECO_UUID1;
//            beacon.latitude = 126.9665150;
//            beacon.longtitude = 37.5444176;
//            beacon.location = "신한 은행 숙대점.앞. 공사 현장";
//            mBeacons.add(beacon);
//            sendStartMessageToMain(beacon);

            // Beacon beacon1 = new Beacon();
            // beacon.uuid = Constances.RECO_UUID1;
        }
    }

    @Override
    public void didDetermineStateForRegion(RECOBeaconRegionState recoBeaconRegionState, RECOBeaconRegion recoBeaconRegion) {
        LogManager.d(TAG, "didDetermineStateForRegion()");
        LogManager.d(TAG, "didDetermineStateForRegion() " + " - " + recoBeaconRegionState);
        LogManager.d(TAG, "didDetermineStateForRegion() "  + recoBeaconRegion.getProximityUuid());
//        if (!isStart) {
//            isStart = false;
//            //beacon 만들어서 넣기 !
//            LogManager.d(TAG, "didStartMonitoringForRegion() - " + recoBeaconRegion.getProximityUuid());
//            Beacon beacon = new Beacon();
//            beacon.uuid = Constances.RECO_UUID1;
//            beacon.latitude = 126.9665150;
//            beacon.longtitude = 37.5444176;
//            beacon.location = "신한 은행 숙대점.앞. 공사 현장";
//            sendStartMessageToMain(beacon);
//            //Write the code when the state of the monitored region is changed
//        }
    }

    @Override
    public void monitoringDidFailForRegion(RECOBeaconRegion recoBeaconRegion, RECOErrorCode recoErrorCode) {
        LogManager.d(TAG, "monitoringDidFailForRegion()");

    }

    class AsynchTask extends AsyncTask<String, Void, String> {
        private String serverUrl = Constances.SERVER_URL;
        private String subUrl = Constances.SUB_URL;
        String data;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LogManager.d(TAG,"onPreExecute()");
            //isError = false;
        }

        @Override
        protected String doInBackground(String... params) {
            //server로부터 데이터 얻어오기
            //prepare send url
            serverUrl += subUrl + params[0];
            LogManager.d(TAG, "Get URL : " + serverUrl);
            //http
            try {
                URL url = new URL(serverUrl);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                InputStream is = null;
                try {
                    is = conn.getInputStream();
                } catch (Exception e) {
                    LogManager.d("phj", e.toString());
                }
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr);
                StringBuilder responseData = new StringBuilder();
                // Read Server Response
                while ((data = reader.readLine()) != null) {
                    responseData.append(data);
                }
                return responseData.toString();
            } catch (Exception e) {
                //isError = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            LogManager.d(TAG, "s : " + s);
            if (s != null) {
                Beacon beacon = BeaconJsonParser.parse(s);
                if(!mBeaconsUuidList.equals(beacon.uuid)) {
                    mBeaconsUuidList.add(beacon.uuid);
                    sendStartMessageToMain(beacon);
                }else{
                    LogManager.d(TAG,"이미 거리 계산을 실행 중입니다..");
                }
            } else {
                LogManager.d(TAG, "error : JsonObject null");
            }
        }
    }


    private void sendStartMessageToMain(Beacon data){
        LogManager.d(TAG,"MyService send start Message to MainActivity...........");
        if(mainHandler!=null) {
            Message msg = mainHandler.obtainMessage();
            if (isError == false && msg != null) {
                msg.what = 200;
                msg.obj = data;
            } else {
                msg.what = 100;
                msg.obj = "Server Error";
            }
            mainHandler.sendMessage(msg);
        }
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    private void sendStopMessageToMain(){
        LogManager.d(TAG,"MyService send stop Message to MainActivity...........");
        Message msg = mainHandler.obtainMessage();
        msg.what = 300;
        mainHandler.sendMessage(msg);
    }

    private void tearDown() {
        LogManager.d(TAG, "tearDown()");
        this.stopMonitoring();
        mBeaconsUuidList.clear();
        sendStopMessageToMain();
        try {
            mRecoManager.unbind();
        } catch (RemoteException e) {
             LogManager.d(TAG, "RemoteException has occured while executing unbind()");
            e.printStackTrace();
        }
    }
}
