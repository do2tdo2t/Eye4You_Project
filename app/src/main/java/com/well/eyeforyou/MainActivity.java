package com.well.eyeforyou;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.well.eyeforyou.caclulator.AzimuthCaculator;
import com.well.eyeforyou.caclulator.DirectionAngleCalculator;
import com.well.eyeforyou.service.SSTService;
import com.well.eyeforyou.util.BeaconAlarmManager;
import com.well.eyeforyou.util.ConnectivityUtil;
import com.well.eyeforyou.component.Constances;
import com.well.eyeforyou.caclulator.DistanceCalculator;
import com.well.eyeforyou.component.KalmanFilter;
import com.well.eyeforyou.util.LogManager;
import com.well.eyeforyou.util.PoisAlarmManager;
import com.well.eyeforyou.timer.SSTTimer;
import com.well.eyeforyou.timer.TTSTimer;
import com.well.eyeforyou.listener.OnFlingGestureListener;
import com.well.eyeforyou.service.BeaconBackgroundMonitoringService;
import com.well.eyeforyou.service.POIService;
import com.well.eyeforyou.service.TTSService;
import com.well.eyeforyou.vo.Beacon;
import com.well.eyeforyou.vo.Poi;
import com.well.eyeforyou.vo.Pois;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnTouchListener, SensorEventListener {

    private final static String TAG = "MainActivity";
    public static String DIRECTION;

    private boolean isStartedBackgroundService = false;
    private boolean isStartedPoiService = false;
    private boolean isStartedTTSService = false;
    private boolean isBoundBackgroundService = false;
    private boolean isBoundPoiSerivce = false;
    private boolean isBoundTTSService = false;

    private boolean isBoundSSTService = false;


    public static double current_Latitude;
    public static double current_Longitude;

    private double mDist = 0.0 ;
    private SSTTimer sstTimer = null;
    private static String sstResult;
    KalmanFilter mKalman = null;

    private ViewSwitcher view;
    private ImageView mImgView;

    private SensorManager mSensorManager;

    private float current_azimuth;
    private float temp_azimuth;
    private Sensor mAccSensor, mMagSensor;
    private float[] mAccData = null;
    private float[] mMagData = null;
    private float[] mRotation = new float[9];
    private float[] mResultData = new float[3];

    private static String alarm;

    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private TTSTimer ttsTimer;
    private static boolean isAppDestroying = false;
    private long keyPressLastTime = -1;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            if(keyPressLastTime == -1){
                keyPressLastTime = System.currentTimeMillis();
            }else {
                long gab = System.currentTimeMillis() - keyPressLastTime;
                if (gab < 90 && !isAppDestroying ) {
                    isAppDestroying = true;
                    finishApp();
                    keyPressLastTime = -1;
                } else {
                    LogManager.d(TAG, "PHJ" + " oneTab");
                    keyPressLastTime = System.currentTimeMillis();
                }
            }
        }else if(event.getKeyCode() == KeyEvent.KEYCODE_HOME){
            moveTaskToBack(true);
        }else if(event.getKeyCode()==KeyEvent.KEYCODE_VOLUME_DOWN){
            //음성인식 멈춤
            Message msg = new Message();
            if (mTTSMessenger != null) {
                msg.what = Constances.MSG_BEQUIET;
                try {
                    mTTSMessenger.send(msg);
                } catch (RemoteException e) {
                    LogManager.d(TAG, "error : " + e.toString());
                }
            } else {
                LogManager.v(TAG, "mTTSMessenger is null");
            }
        }else if(event.getKeyCode()==KeyEvent.KEYCODE_VOLUME_UP){
            //안내 메시지 시작
            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(100);
            sendMessageToTTS(getString(R.string.direction),Constances.TTS_STOP_SAY);
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DIRECTION = getString(R.string.direction);

        mImgView = (ImageView) findViewById(R.id.imgTranslate);
        final Animation animTransRight = AnimationUtils.loadAnimation(
                this, R.anim.anim_translate);
        mImgView.setOnTouchListener(this);

        mImgView.startAnimation(animTransRight);
        view = (ViewSwitcher) findViewById(R.id.ViewSwitcher);
        view.setOnTouchListener(this);

        ttsTimer = new TTSTimer();
        startTTSService();
        bindSSTService();

        buildGoogleApiClient();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        updateAngleTime = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager != null && mMagSensor != null && mAccSensor != null) {
            mSensorManager.registerListener(this, mAccSensor, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mMagSensor, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
            mKalman = new KalmanFilter(mResultData[0]);
        }

        if (ConnectivityUtil.getInternetConnectivityState(MainActivity.this)&&!mGoogleApiClient.isConnected()) {
            LogManager.d(TAG,"google service fused start..........");
            mGoogleApiClient.connect();
        }

        if (this.isServiceRunning(this)) {
            sendMessageToTTS("서비스가 실행 중입니다.",Constances.TTS_NEXT_SAY);
        } else {
            sendMessageToTTS("서비스가 꺼져있습니다.",Constances.TTS_NEXT_SAY);
        }
    }


    private boolean isServiceRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningService : am.getRunningServices(Integer.MAX_VALUE)) {
            if (BeaconBackgroundMonitoringService.class.getName().equals(runningService.service.getClassName())) {
                isStartedBackgroundService = true;
            }
            if (POIService.class.getName().equals(runningService.service.getClassName())) {
                isStartedPoiService = true;
            }
            if(mBeaconService != null){
                isBoundBackgroundService = true;
            }
            if(mPOIService != null){
                isBoundPoiSerivce = true;
            }
        }
        LogManager.d(TAG, " " + isStartedPoiService + " " + isStartedBackgroundService + isBoundBackgroundService + isBoundPoiSerivce);
        if (isStartedPoiService && isStartedBackgroundService && isBoundPoiSerivce && isBoundBackgroundService) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endAllService();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        mSensorManager.unregisterListener(this);
        isAppDestroying = false;
    }


    public void startAllService() {
        LogManager.d(TAG, "startAllService()");
        startBeaconBackgroundMonitoringService();
        startPOIService();
        view.showNext();
        moveTaskToBack(true);
    }

    public void unbindAllService(){
        LogManager.d(TAG, "unbindAllService()");
        try {
            if(mBeaconService != null) {
                unbindService(mBeaconConnection);
                isBoundBackgroundService = false;
                mBeaconService = null;
            }else{
                LogManager.d(TAG, "unbindAllService MBS is null");
            }
            if(mPOIService != null) {
                unbindService(mPOIConnection);
                isBoundPoiSerivce = false;
                mPOIService = null;
            }else{
                LogManager.d(TAG, "unbindAllService PBS is null");
            }
        }catch (Exception e){
            LogManager.d("unbindAllService()","error : "+ e.toString());
        }
        LogManager.d(TAG, "unbindAllService() "+ isBoundBackgroundService + isBoundPoiSerivce);
    }

    public void endAllService() {
        //view.showPrevious();
        try {
            endBeaconBackgroundMonitoringService();
            endPOIService();
            endTTSService();
        }catch (Exception e){
           LogManager.d(TAG, e.toString());
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent me) {
        if (flingListener.onTouch(view, me)) {
            return false;
        }
        if (me.getAction() == MotionEvent.ACTION_DOWN) {

        }
        return true;
    }

    OnFlingGestureListener flingListener = new OnFlingGestureListener() {
        public void onRightToLeft() {
            LogManager.d(TAG, "onRightToLeft");
        }

        public void onLeftToRight() {
            LogManager.d(TAG, "onLeftToRight");
            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(100);
            if (isStartedBackgroundService && isStartedPoiService && isBoundBackgroundService && isBoundPoiSerivce) {
                sendMessageToTTS("서비스가 종료 되었습니다.",Constances.TTS_STOP_SAY);
                unbindAllService();
                view.showPrevious();
            } else if(checkConnectivity()){
                if(!mGoogleApiClient.isConnected()||!mGoogleApiClient.isConnecting()){
                    mGoogleApiClient.connect();
                }
                if(!isStartedBackgroundService && !isStartedPoiService && !isBoundBackgroundService && !isBoundPoiSerivce){
                    startAllService();
                }else if(isStartedBackgroundService && isStartedPoiService && !isBoundBackgroundService && !isBoundPoiSerivce){
                    bindPOIService(new Intent(MainActivity.this, POIService.class));
                    bindBeaconBackgroundMonitoringService(new Intent(MainActivity.this, BeaconBackgroundMonitoringService.class));
                    view.showNext();
                    moveTaskToBack(true);
                }else {
                    endAllService();
                    startAllService();
                }
                sendMessageToTTS("서비스를 실행하였습니다.",Constances.TTS_NEXT_SAY);
            }
        }

        public void onBottomToTop() {
            LogManager.d(TAG, "onBottomToTop");
            boolean isMobileEnable = ConnectivityUtil.getInternetConnectivityState(MainActivity.this);
            if (isMobileEnable) {
                if(isBoundSSTService) {
                    ttsTimer.say(mTTSMessenger, getString(R.string.sst_start));
                    mSSTService.startListening();
                }else{
                    sendMessageToTTS("음성 인식을 정상적으로 실행 할 수 없습니다.",Constances.TTS_STOP_SAY);
                }
            }
        }
    };

    private void finishApp() {
        if(ttsTimer == null){
            ttsTimer = new TTSTimer();
        }
        ttsTimer.say(mTTSMessenger,getString(R.string.app_finish));
        TimerTask timertask = new TimerTask() {
            public void run() {
                endAllService();
                unbindSSTService();
                moveTaskToBack(true);
                finish();
            }
        };
        ttsTimer.schedule(timertask,3000);
    }

    public boolean checkConnectivity() {
        //Connectivity 검사
        boolean bleStatus = ConnectivityUtil.getBluetoothConnectivityState(getApplicationContext());
        boolean gpsStatus = ConnectivityUtil.getGPSConnectivityState(getApplicationContext());
        boolean InternetStatus = ConnectivityUtil.getInternetConnectivityState(getApplicationContext());

        LogManager.d(TAG, "ble status : " + bleStatus + "... gps status : " + gpsStatus + "... Internet status : " + InternetStatus);
        if (!bleStatus) {
            sendMessageToTTS(getString(R.string.ble_off),Constances.TTS_STOP_SAY);
            return false;
        }
        if (!gpsStatus) {
            sendMessageToTTS(getString(R.string.gps_off),Constances.TTS_STOP_SAY);
            return false;
        }
        if(!InternetStatus){
            sendMessageToTTS(getString(R.string.moblie_off),Constances.TTS_STOP_SAY);
            return false;
        }
        return true;
    }

    private void startTTSService(){
        Intent i = new Intent(this,TTSService.class);
        startService(i);
        isStartedTTSService = true;
        bindService(i, mTTSConnection, Context.BIND_AUTO_CREATE);

    }

    private void startBeaconBackgroundMonitoringService() {
        Intent intent = new Intent(this, BeaconBackgroundMonitoringService.class);
        startService(intent);
        isStartedBackgroundService = true;
        bindBeaconBackgroundMonitoringService(intent);
    }

    private void startPOIService() {
        Intent intent = new Intent(this, POIService.class);
        startService(intent);
        isStartedPoiService = true;
        bindPOIService(intent);
    }
    private void bindSSTService(){
        Intent intent = new Intent(this, SSTService.class);
        boolean val = bindService(intent, mSSTConnection, Context.BIND_AUTO_CREATE);
        if (val) {
            LogManager.d(TAG, "SSTService bind service success .......");
        }
    }
    private void unbindSSTService(){
        if(isBoundSSTService)
            unbindService(mSSTConnection);
    }

    private void bindBeaconBackgroundMonitoringService(Intent intent) {
        boolean val = false;
        val = bindService(intent, mBeaconConnection, Context.BIND_AUTO_CREATE);
        if (val) {
            isBoundBackgroundService = true;
            LogManager.d(TAG, "BBMS bind service success .......");
        }else{
            LogManager.d(TAG, "BBMS bind service fail .......");
        }
    }

    private void bindPOIService(Intent intent) {
        boolean val = false;
        val = bindService(intent, mPOIConnection, Context.BIND_AUTO_CREATE);
        if (val) {
            isBoundPoiSerivce = true;
            LogManager.d(TAG, "bind service success .......");
        }else{
            LogManager.d(TAG, "BBMS bind service fail .......");
        }
    }


    private void endBeaconBackgroundMonitoringService() {
        Intent intent = new Intent(this, BeaconBackgroundMonitoringService.class);
        LogManager.d(TAG, "endRecoBackgroundMonitoringService");
        if(isBoundBackgroundService)
            unbindService(mBeaconConnection);
        if(isStartedBackgroundService) {
            stopService(intent);
            isStartedBackgroundService = false;
        }
    }

    private void endPOIService() {
        Intent intent = new Intent(this, POIService.class);
        LogManager.d(TAG, "end POIService");
        if(isBoundPoiSerivce)
            unbindService(mPOIConnection);

        if(isStartedPoiService) {
            stopService(intent);
            isStartedPoiService = false;
        }
    }

    private void endTTSService(){
        Intent intent = new Intent(this, TTSService.class);
        LogManager.d(TAG, "end TTSService");
        if(isBoundTTSService) {
            unbindService(mTTSConnection);
            isBoundTTSService = false;
        }
        if (isStartedTTSService) {
            stopService(intent);
            isStartedTTSService =false;
        }
    }

    BeaconBackgroundMonitoringService mBeaconService;
    //Service Connection
    private ServiceConnection mBeaconConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            BeaconBackgroundMonitoringService.LocalBinder binder = (BeaconBackgroundMonitoringService.LocalBinder) service;
            mBeaconService = binder.getService(forBeaconServiceHandler);
            LogManager.d(TAG,mBeaconService.toString().toString());

            isBoundBackgroundService = true;
            LogManager.d(TAG, " recobeacon service connected....");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogManager.d(TAG, componentName.toString() + " service disconnected....");

            mBeaconService = null;
            isBoundBackgroundService = false;
            LogManager.d(TAG, componentName.toString() + " service disconnected....");

        }
    };


    public boolean isRegion =  false;
    private Handler forBeaconServiceHandler = new Handler() {
        public void handleMessage(Message msg) {
            //LogManager.d(TAG, "Handle Message from Service............. " + '\n' + "msg = [" + (String) msg.obj + "]");
            switch (msg.what) {
                case 100:
                    sendMessageToTTS(getString(R.string.moblie_off),Constances.TTS_STOP_SAY);
                case 200:
                    isRegion = true;
                    if (isStartedBackgroundService && isBoundBackgroundService) {
                        Beacon mBeacon = (Beacon) msg.obj;
                        //mBeacon = BeaconJsonParser.parse((String) msg.obj);
                        mBeacon.isFirstAlarm = false;
                        //distance 계산 시작
                        DangerAlarmTask task = new DangerAlarmTask();
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mBeacon);
                    }
                    break;
                case 300:
                    //region 에서 벗어남.
                    isRegion = false;
                    LogManager.d(TAG, "DangerAlarmAsynch() didExitRegion !");
                    break;
                default:
                    break;
            }
        }
    };

    POIService mPOIService;
    private ServiceConnection mPOIConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            POIService.LocalBinder binder = (POIService.LocalBinder) service;
            mPOIService = binder.getService(forPOIServiceHandler);
            LogManager.d(TAG,mPOIService.toString().toString());

            isBoundPoiSerivce = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mPOIService = null;
            isBoundPoiSerivce = false;
        }
    };

    public static Messenger mTTSMessenger;
    private ServiceConnection mTTSConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mTTSMessenger = new Messenger(service);
            isBoundTTSService = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mTTSMessenger = null;
            isBoundTTSService = false;
        }
    };

    private SSTService mSSTService;
    private ServiceConnection mSSTConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SSTService.LocalBinder binder = (SSTService.LocalBinder) service;
            mSSTService = binder.getService(mSSTHandler);
            isBoundSSTService = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mTTSMessenger = null;
            isBoundSSTService = false;
        }
    };

    public Handler mSSTHandler = new Handler() {
        public void handleMessage(Message fromMsg) {
            sstTimer = null;
            switch (fromMsg.what) {
                case 200:
                    sstResult = (String) fromMsg.obj;
                    sendMessageToTTS(sstResult + getString(R.string.sst_excute),Constances.TTS_NEXT_SAY);
                    try {
                        String category = (String) URLEncoder.encode((String) fromMsg.obj, "utf-8");
                        String query = "&centerLat=" + current_Latitude + "&centerLon=" + current_Longitude + "&categories=" + category;
                        mPOIService.startPOIAsynchTask(query, true);
                    } catch (UnsupportedEncodingException e) {
                        LogManager.e(TAG," error : "+e.toString());
                    }
                    break;
                //일치하는 결과가 없음.
                case 201:
                    sstResult = (String) fromMsg.obj;
                    sendMessageToTTS(sstResult + " 검색을 시작합니다" ,Constances.TTS_STOP_SAY);
                    String query = "&centerLat=" + current_Latitude + "&centerLon=" + current_Longitude;
                    mPOIService.startPOIAsynchTask(query, false);
                    break;
                case 300:
                    LogManager.e(TAG, "sst error code : " + fromMsg.arg1);
                    if (fromMsg.arg1 == 6) {
                        sendMessageToTTS(getString(R.string.sst_error_no_voice),Constances.TTS_NEXT_SAY);
                    } else {
                        sendMessageToTTS(getString(R.string.sst_error),Constances.TTS_NEXT_SAY);
                    }
                    break;
            }
        }
    };



    public Handler forPOIServiceHandler = new Handler() {
        public void handleMessage(Message fromMsg) {
            Poi mPoi;
            Pois mPois = (Pois)fromMsg.obj;
            String alarm = "";
            switch (fromMsg.what) {
                case Constances.POI_MSG_OK1:
                    //category 있음
                    LogManager.d(TAG," forPOIServiceHandler OK1 " + mPois.totalCount);
                    mPoi = PoisAlarmManager.getNearestPoi(current_Latitude,current_Longitude,mPois);
                    if(mPoi==null){
                        alarm +="60m 이내 시설이 없습니다";
                        sendMessageToTTS(alarm,Constances.TTS_NEXT_SAY);
                    }else{
                        alarm += PoisAlarmManager.makeStringForNotification(current_Latitude, current_Longitude, current_azimuth, mPoi);
                        sendMessageToTTS(alarm,Constances.TTS_NEXT_SAY);
                    }
                    break;
                case Constances.POI_MSG_OK2:
                    LogManager.d(TAG," forPOIServiceHandler OK2 " + mPois.totalCount);
                    mPoi = PoisAlarmManager.getEqualPoitoResult(current_Latitude,current_Longitude,mPois,sstResult);
                    if(mPoi==null){
                        alarm +="60m 이내 시설이 없습니다";
                        sendMessageToTTS(alarm,Constances.TTS_NEXT_SAY);
                    }else{
                        alarm += PoisAlarmManager.makeStringForNotification(current_Latitude, current_Longitude, current_azimuth, mPoi);
                        sendMessageToTTS(alarm,Constances.TTS_NEXT_SAY);
                    }
                    break;
            }
        }
    };

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    //google fused service
    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100);


        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            LogManager.d(TAG, "onConnected " + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
        }else{
            LogManager.d(TAG,"Last location is null...");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    public double pre_lat;
    public double pre_lon;
    static double updateAngleTime = 0.0;
    public static float angle;
    @Override
    public void onLocationChanged(Location location) {
        current_Latitude = location.getLatitude();
        current_Longitude = location.getLongitude();
        LogManager.d(TAG,current_Latitude+" "+current_Longitude);
        Toast.makeText(MainActivity.this,mDist+"\n"+current_azimuth ,Toast.LENGTH_SHORT).show();
    }

    private void updateCurrentAngle(){
        double mAngle = DirectionAngleCalculator.getCorrDirection(current_Latitude,current_Longitude,pre_lat,pre_lon);
        if(mAngle == 0){
            //Toast.makeText(MainActivity.this," yet update... "+'\n'+" acc : "+current_acc_azimuth+ "\n ori : "+current_azimuth,Toast.LENGTH_SHORT).show();
        }else if(mAngle <= 360.0){
            angle = (Float.valueOf(String.valueOf(mAngle)));
            //Toast.makeText(MainActivity.this," update ! \nangle : "+angle+"\n acc : "+current_acc_azimuth+ "\n ori : "+ current_azimuth,Toast.LENGTH_SHORT).show();
        }

        LogManager.d(TAG,"angel: "+angle);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        LogManager.d(TAG,"onConnectionFailed "+ connectionResult.toString());
        buildGoogleApiClient();
    }
    private long angleUpdateLastTime;
    private long shakelastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;
    private static float current_acc_azimuth;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                whenShakeEvent(sensorEvent);
                mAccData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_ORIENTATION:
                temp_azimuth = sensorEvent.values[0];
                break;
        }
        if (mAccData != null && mMagData != null) {
            mSensorManager.getRotationMatrix(mRotation, null, mAccData, mMagData);
            mSensorManager.getOrientation(mRotation, mResultData);

            mResultData[0] = (float) Math.toDegrees(mResultData[0]);
            if (mResultData[0] < 0) mResultData[0] += 360;

            float acc_azimuth = AzimuthCaculator.getCorrAccMagVal(mResultData[0]);
            float ori_azimuth = AzimuthCaculator.getCorrOrientationVal(temp_azimuth);
            if (ori_azimuth != 0) {
                current_azimuth = ori_azimuth;
            }
            if(acc_azimuth != 0){
                current_acc_azimuth = acc_azimuth;
            }
        }
    }
    public synchronized void whenShakeEvent(SensorEvent sensorEvent){
        long currentTime = System.currentTimeMillis();
        long gabOfTime = (currentTime - shakelastTime);
        if(gabOfTime > 100){
            shakelastTime = currentTime;
            x = sensorEvent.values[SensorManager.DATA_X];
            y = sensorEvent.values[SensorManager.DATA_Y];
            z = sensorEvent.values[SensorManager.DATA_Z];
            speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;
            if(speed > Constances.SHAKE_THRESHOLD){
                LogManager.d("shake","event");
            }
            lastX = sensorEvent.values[DATA_X];
            lastY = sensorEvent.values[DATA_Y];
            lastZ = sensorEvent.values[DATA_Z];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    class DangerAlarmTask extends AsyncTask<Beacon, Void, Void> {
        private Beacon mBeacon;
        double dist;
        double true_bearing;
        double relative_degree;
        String notification_string;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LogManager.d(TAG, "startDistanceCaculate()");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Void doInBackground(Beacon... params) {
            LogManager.d(TAG, "doInBackground()");
            mBeacon = (Beacon) params[0];
            mBeacon.isFirstAlarm = false;
            //distance 계산
            double dist;
            double relative_degree;
            long timeMillis = System.currentTimeMillis();

            LogManager.d(TAG, mBeacon.location + " " + mBeacon.latitude + " " + mBeacon.longtitude + " "+ current_Longitude + " "+ current_Latitude);

            if (mBeacon.longtitude * mBeacon.latitude * current_azimuth * current_Latitude * current_Longitude == 0) {
                LogManager.d(TAG, "null data ....");
                dist = 10000.0;
                relative_degree = 0;
            } else {
                dist = DistanceCalculator.calculateDistance(mBeacon, current_Latitude, current_Longitude);
                relative_degree = DistanceCalculator.caculateRelativeDegree(current_Latitude, current_Longitude, mBeacon.latitude, mBeacon.longtitude, current_azimuth);
                LogManager.d(TAG, "dist : [ " + dist + " ]");

            }

            while(!mBeacon.isFirstAlarm) {
                if (System.currentTimeMillis() - timeMillis >= 10) {
                    if (BeaconAlarmManager.isDangerFoward(relative_degree) && BeaconAlarmManager.isDangerNearBy(dist)) {

                        notification_string = BeaconAlarmManager.makeStringForNotification(dist, relative_degree, mBeacon.location);
                        mBeacon.isFirstAlarm = true;
                        LogManager.d("Notification", notification_string);
                        //진동 후 알람
                        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibe.vibrate(200);
                        Vibrator vibe1 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibe1.vibrate(200);
                        Vibrator vibe2 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibe2.vibrate(200);
                        sendMessageToTTS(notification_string, Constances.TTS_STOP_SAY);

                    }

                    dist = DistanceCalculator.calculateDistance(mBeacon, current_Latitude, current_Longitude);
                    mDist = dist;
                    relative_degree = DistanceCalculator.caculateRelativeDegree(current_Latitude, current_Longitude, mBeacon.latitude, mBeacon.longtitude, current_azimuth);

                }
                timeMillis = System.currentTimeMillis();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            LogManager.d(TAG, "DangerAlarm- onPostExecute()");
        }
    }

    public void sendMessageToTTS(String str , int arg1) {
        Message msg = new Message();
        if (mTTSMessenger != null) {
            msg.what = Constances.MSG_SAY;
            msg.obj = str;
            msg.arg1 = arg1;
            try {
                mTTSMessenger.send(msg);
            } catch (RemoteException e) {
                LogManager.d(TAG, "error : " + e.toString());
            }
        } else {
            LogManager.v(TAG, "mTTSMessenger is null");
        }
    }
}
