package com.well.eyeforyou.util;


import com.well.eyeforyou.vo.NumMap;

public class BeaconAlarmManager {
    private final static String TAG = "BeaconAlarmManager";
    public static boolean isDangerFoward(double relative_degree) {
        if ((relative_degree >= -110.0 && relative_degree <= 110.0)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isDangerNearBy(double dist) {
        if (dist <= 15.0 && dist >= 0.0) {
            return true;
        } else {
            return false;
        }
    }
    public static String makeStringForNotification(double dist, double relative_degree, String beacon_region) {
        LogManager.d(TAG, "makeStringForNotification()");
        String str = "";

        if (relative_degree <= -40.0 && relative_degree >=-110.0) {
            str += "열시 방향 ";
        } else if (relative_degree >= 40.0 && relative_degree <= 110.0) {
            str += "두시 방향 ";
        } else if (relative_degree < 40.0 && relative_degree > -40.0) {
            str += "앞쪽 ";
        }

        String walk = changeDistToWalk(dist);

        str += walk + " 걸음 앞에 " + beacon_region + " 있습니다.";
        return (str);
    }

    public static String changeDistToWalk(double dist){
        String walk ="";

        dist = dist * 1.2;
        dist = Math.floor(dist);
        NumMap numMap = new NumMap();
        if (numMap.map.containsKey(dist)) {
            walk = numMap.map.get(dist);
        }

        LogManager.d(TAG,"walk "+ walk);
        return walk;
    }
}
