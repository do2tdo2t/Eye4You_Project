package com.well.eyeforyou.component;

/**
 * Created by 박효정 on 2016-08-02.
 */
public class Constances {
    //This is a default proximity uuid of the RECO
    public static final String SERVER_URL = "http://52.32.2.175:8080/EyeForYouServer/getBeacon.jsp";
    public static final String SUB_URL = "?uuid=";

    public final static String TmapURL = "https://apis.skplanetx.com/tmap/pois/search/around?version=1&radius=1&reqCoordType=WGS84GEO&resCoordType=WGS84GEO&count=150";
    public final static String AppKey = "bde1361b-740d-3198-b398-2f71766d84ea";

    public static final int POI_MSG_OK1 = 200;
    public static final int POI_MSG_OK2 = 201;

    public static final int MSG_OK = 200;
    public static final int MSG_ERR = 300;

    public static final double POI_DIST_LIMIT = 0.06;
    public static final int POI_MAX = 150;

    public static final int TTS_NEXT_SAY = 1;
    public static final int TTS_STOP_SAY = 2;



    public static final String RECO_UUID1 = "24DDF4118CF1440C87CDE368DAF9C93E";
    public static final String RECO_UUID2 = "24DDF411-8CF1-440C-87CD-E368DAF9C94E";

    public static final boolean SCAN_RECO_ONLY = true;

    public static final boolean ENABLE_BACKGROUND_RANGING_TIMEOUT = true;

    public static final boolean DISCONTINUOUS_SCAN = false;

    public static final int SHAKE_THRESHOLD = 800;

    public static final int MSG_SAY = 1;
    public static final int MSG_BEQUIET = 2;


    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_LOCATION = 10;


    public static final int SWIPE_MIN_DISTANCE = 120;
    public static final int SWIPE_MAX_OFF_PATH = 250;
    public static final int SWIPE_THRESHOLD_VELOCITY = 200;


    public static final long mScanDuration = 1*1000L;
    public static final long mSleepDuration = 10*1000L;
    public static final long mRegionExpirationTime = 120*1000L;
    public static final int mNotificationID = 9999;

}
