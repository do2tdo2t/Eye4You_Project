package com.well.eyeforyou.util;

import com.well.eyeforyou.caclulator.DistanceCalculator;
import com.well.eyeforyou.component.Constances;
import com.well.eyeforyou.vo.Poi;
import com.well.eyeforyou.vo.Pois;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 박효정 on 2016-08-20.
 */
public class PoisAlarmManager {
    //앞에 있는지 아닌지.
    //check -> Name,
    private static final String TAG = "PoisAlarmManager";

    private static String checkDirection(double currentLat, double currentLon, float azimuth, Poi poi) {
        double relative_degree;

        relative_degree = DistanceCalculator.caculateRelativeDegree(currentLat, currentLon,poi.frontLat, poi.frontLon, azimuth);

        if((relative_degree >= -140.0 && relative_degree <=0.0) || (relative_degree<=140.0 && relative_degree >=0.0)){
            if (relative_degree <= -40.0 && relative_degree >= -140.0 ) {
                return "전방 왼쪽 ";
            } else if (relative_degree > 40.0 && relative_degree <=140.0) {
                return "전방 오른쪽 ";
            } else if (relative_degree < 40.0 && relative_degree > -40.0) {
                return "앞 쪽";
            }
        }
        return "뒤 편";
    }

    public static String makeStringForNotification(double currentLat, double currentLon, float azimuth, Poi poi) {
        String direction;
        double dist;
        String alarm;
        dist = DistanceCalculator.calculateDistance1(poi.frontLat,poi.frontLon,currentLat,currentLon);
        dist = Math.floor(dist);

        direction = checkDirection(currentLat,currentLon,azimuth,poi);
        LogManager.d(TAG,"makeStringForNotification()" + " 거리 "+dist +", 방향 "+ direction);

        if(direction != null) {
            if((0 <= dist) && (dist <= 60)) {
             alarm =direction+" " + String.format("%.0f",dist) + "미터 이내에 " + poi.name + " 위치해 있습니다.";
            return alarm;
            }else {
                return "60미터 이내에 시설물이 없습니다.";
            }
        }else{
            return "오류 발생으로 검색을 종료합니다.";
        }
    }
    public static Poi nearestPoi = null;
    public static Poi getNearestPoi(double lat, double lon, Pois pois){
        LogManager.d(TAG,"getNearestPoi()");
        Poi mPoi;
        Pois mPois = pois;
        double limit_radius;
        double min_dist = 10000;
        double dist;
        int i = 0;
        ArrayList<Poi> list = new ArrayList<>();
        LogManager.d(TAG,"getNearestPoi() "+"pois.totalCount "+ pois.totalCount);
        mPoi = mPois.poiList.get(i);
        limit_radius = Double.valueOf(mPoi.radius);
        LogManager.d(TAG,"getNearestPoi() "+"limit_radius"+limit_radius);

        if( limit_radius <= Constances.POI_DIST_LIMIT ){
            for( ; Double.valueOf(mPoi.radius)<=limit_radius && i < 150  ; i++ ) {
                mPoi = mPois.poiList.get(i);
                //dist = DistanceCalculator.calculateDistance1(lat,lon,mPoi.frontLat,mPoi.frontLat);
                list.add(i,mPoi);
                LogManager.d(TAG,"getNearestPoi() "+mPoi.name);
            }
        }else{
            return null;
        }
        nearestPoi = list.get(0);
        limit_radius = Double.valueOf(nearestPoi.radius);
        for(i = 0  ; i < list.size() && Double.valueOf(list.get(i).radius) <= limit_radius ; i++){
            dist = DistanceCalculator.calculateDistance1(lat,lon,list.get(i).frontLat,list.get(i).frontLon);
            LogManager.d(TAG," dist " + dist);
            if(min_dist > dist ){
                min_dist = dist;
                nearestPoi = list.get(i);
            }
        }
        LogManager.d(TAG,"nearestPoi "+nearestPoi.radius+" "+ nearestPoi.name);
        //가장 앞에 있는 것 choice
        //1. 거리계산 60 이내인지 계산
        //2. 60m 이내일 경우 m 저장해서 비교
        return nearestPoi;
    }
    public static Poi getEqualPoitoResult(double lat, double lon, Pois pois, String sstResult){
        Poi mPoi;
        Pois mPois = pois;
        ArrayList<Poi> list = new ArrayList<>();
        double dist;
        int i;
        double min_dist = 100000;
        boolean v =false;
        try {
            mPoi = mPois.poiList.get(0);
            for (i = 0 ; Double.valueOf(mPoi.radius) <= Constances.POI_DIST_LIMIT; i++) {
                mPoi = mPois.poiList.get(i);
                if (mPoi.name.contains(sstResult)) {
                    LogManager.d(TAG,"getEqualPoitoResult"+" "+mPoi.name);
                    //dist = DistanceCalculator.calculateDistance1(lat,lon,mPoi.frontLat, mPoi.frontLon);
                    list.add(mPoi);
                    v = true;
                }
            }
            if (!v) {
                return  null;
            }
        }catch (NullPointerException e){
            LogManager.d(TAG,"error + : "+e.toString());
        }

        //hashmap 비교
        nearestPoi = list.get(0);
        min_dist = 10000;
        for(i = 0 ; i < list.size() ; i++){
            dist = DistanceCalculator.calculateDistance1(lat,lon,list.get(i).frontLat,list.get(i).frontLon);
            if(min_dist > dist ){
                min_dist = dist;
                nearestPoi = list.get(i);
            }
        }
        return  nearestPoi;
    }
}
