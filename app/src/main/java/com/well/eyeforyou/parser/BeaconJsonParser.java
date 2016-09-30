package com.well.eyeforyou.parser;

import com.google.gson.Gson;
import com.well.eyeforyou.util.LogManager;
import com.well.eyeforyou.vo.Beacon;

/**
 * Created by 박효정 on 2016-08-06.
 */
public class BeaconJsonParser {
    private static final String TAG = "BeaconJsonParser";
    public static Beacon parse(String object){
        LogManager.d(TAG,"start parsing .....................");
        Gson gson = new Gson();
        Beacon beacon = gson.fromJson(object, Beacon.class);
        beacon.toString();
        return beacon;
    }
}
