package com.well.eyeforyou.parser;

import android.util.Log;

import com.well.eyeforyou.component.Constances;
import com.well.eyeforyou.util.LogManager;
import com.well.eyeforyou.vo.Poi;
import com.well.eyeforyou.vo.Pois;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 박효정 on 2016-08-12.
 */
public class PoisJsonParser {
    private static final String TAG = "PoisJsonParser";
    public static Pois parse(JSONObject object) {
        Pois pois = new Pois();
        int totalCount = 0;
        try{
            pois.totalCount = object.getJSONObject("searchPoiInfo").getString("totalCount");
            totalCount = Integer.valueOf(pois.totalCount);
            LogManager.d(TAG,"pois.totalCount:" + pois.totalCount);
            if (object != null) {
                try {
                    Poi poi;
                    JSONArray jsonArray = object.getJSONObject("searchPoiInfo").getJSONObject("pois").getJSONArray("poi");
                    for (int i = 0; i < Constances.POI_MAX && i < totalCount ; i++) {
                            JSONObject temp = jsonArray.getJSONObject(i);
                            poi = new Poi();
                            poi.id = (String) temp.get("id");
                            poi.name = (String) temp.get("name");
                            poi.frontLat = Double.parseDouble((String) temp.get("frontLat"));
                            poi.frontLon = Double.parseDouble((String) temp.get("frontLon"));
                            poi.upperAddrName = (String) temp.get("upperAddrName"); //서울
                            poi.middleAddrName = (String) temp.get("middleAddrName"); //구
                            poi.lowerAddrName = (String) temp.get("lowerAddrName"); // 동
                            poi.rpFlag = (String) temp.get("rpFlag"); // 동
                            poi.telNo = (String) temp.get("telNo");
                            poi.telNo = (String) temp.get("telNo");
                            poi.radius = (String) temp.get("radius"); // 0.02km -> 20m
                            pois.poiList.add(poi);
                            LogManager.v(TAG, "parse() " + " "+poi.name+ " "+poi.radius+ " "+poi.frontLat+ " "+poi.frontLon+ " "+poi.id);
                    }

                } catch (JSONException e) {
                    Log.e(TAG,"error : " + e.toString());
                }
            }else{
                Log.e(TAG, "totalCount : "+ pois.totalCount);
                return null;
            }
        }catch (Exception e){
            Log.e(TAG,"error : " + e.toString());
        }
        return  pois;
    }
}
