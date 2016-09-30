package com.well.eyeforyou.vo;

/**
 * Created by 박효정 on 2016-08-02.
 */
public class Beacon {

    public String uuid;
    public double latitude;
    public double longtitude;
    public String location;
    public boolean isFirstAlarm;

    @Override
    public String toString() {
        return "Beacon{" +
                "uuid='" + uuid + '\'' +
                ", latitude=" + latitude +
                ", longtitude=" + longtitude +
                ", location='" + location + '\'' +
                '}';
    }
}
