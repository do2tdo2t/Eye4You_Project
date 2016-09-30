package com.well.eyeforyou.caclulator;

import android.util.Log;
import android.widget.Toast;

import com.well.eyeforyou.util.LogManager;
import com.well.eyeforyou.vo.Beacon;

/**
 * Created by 박효정 on 2016-06-13.
 */

public class DistanceCalculator {
    private static String TAG = "DistanceCalculator";

    public static double calculateDistance(Beacon beacon ,double lat2, double lon2) {
        double lon1 = beacon.longtitude;
        double lat1 = beacon.latitude;

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        dist = dist*1000;
        LogManager.d(TAG,"dist : [ "+ dist+" ]");
        return (dist);
    }

    public static double calculateDistance1(double beacon_lat, double beacon_lon, double current_lat, double current_lon) {
        double theta = beacon_lon - current_lon;
        double dist = Math.sin(deg2rad(current_lat)) * Math.sin(deg2rad(beacon_lat)) + Math.cos(deg2rad(current_lat)) * Math.cos(deg2rad(beacon_lat)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        dist = dist*1000;
        return (dist);
    }
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static double calculateTrueBearing(double cur_latitude, double cur_longitude,
                                                double dest_latitude, double dest_longitude) {

        double Cur_Lat_radian = cur_latitude * (3.141592 / 180);
        double Cur_Lon_radian = cur_longitude * (3.141592 / 180);

        double Dest_Lat_radian = dest_latitude * (3.141592 / 180);
        double Dest_Lon_radian = dest_longitude * (3.141592 / 180);

        double radian_distance = 0;
        radian_distance = Math.acos(Math.sin(Cur_Lat_radian)
                * Math.sin(Dest_Lat_radian) + Math.cos(Cur_Lat_radian)
                * Math.cos(Dest_Lat_radian)
                * Math.cos(Cur_Lon_radian - Dest_Lon_radian));

        double radian_bearing = Math.acos((Math.sin(Dest_Lat_radian) - Math
                .sin(Cur_Lat_radian)
                * Math.cos(radian_distance))
                / (Math.cos(Cur_Lat_radian) * Math.sin(radian_distance)));

        double true_bearing = 0;
        if (Math.sin(Dest_Lon_radian - Cur_Lon_radian) < 0) {
            true_bearing = radian_bearing * (180 / 3.141592);
            true_bearing = 360 - true_bearing;
        } else {
            true_bearing = radian_bearing * (180 / 3.141592);
        }
        return true_bearing;
    }

    public static double caculateRelativeDegree(double current_Latitude, double current_Longitude , double dest_lat, double dest_lon, float azimuth){
        double relative_degree;
        double true_bearing;
        true_bearing = DistanceCalculator.calculateTrueBearing(current_Latitude, current_Longitude, dest_lat, dest_lon);

        relative_degree = true_bearing - azimuth;

        if(relative_degree >=180){
            relative_degree -=360;
        }else if(relative_degree <= -180){
            relative_degree += 360;
        }

        LogManager.d(TAG, "true_bearing  [ " + true_bearing + " ]"+" current_azimuth  [ " + azimuth + " ]");
        LogManager.d(TAG, "relative degree [ " + relative_degree + " ]");

        return relative_degree;
    }

}
