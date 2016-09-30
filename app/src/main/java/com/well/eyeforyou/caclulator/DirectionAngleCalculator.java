package com.well.eyeforyou.caclulator;

import android.util.Log;

import com.well.eyeforyou.util.LogManager;

/**
 * Created by user on 2016-09-28.
 */
public class DirectionAngleCalculator {
    private static float[] dists = {0, 0, 0, 0, 0};
    private static double[] angles = {0, 0, 0};
    private static int i;
    private static int j;

    public DirectionAngleCalculator() {
        i = 0;
        j = 0;
    }

    public static void initialize() {
        dists[0] = 0;
        dists[1] = 0;
        dists[2] = 0;
        dists[3] = 0;
        dists[4] = 0;

        angles[0] = 0.0;
        angles[1] = 0.0;
        angles[2] = 0.0;
    }

    public static double getCorrDirection(double cur_lat, double cur_lon, double pre_lat, double pre_lon) {
        double result = 0;
        double dist = DistanceCalculator.calculateDistance1(cur_lat ,cur_lon ,pre_lat ,pre_lon);
        double mAngle = DistanceCalculator.calculateTrueBearing(cur_lat, cur_lon, pre_lat, pre_lat);
        LogManager.d("DirectionAngleCalculator"," "+ dist + mAngle);
        if(dist >=3.0 && dist <=100.0){
            return 0.0;
        }else {
            return mAngle;
        }
//        }
//        if (angles[0] == 0) {
//            angles[0] = mAngle;
//        } else {
//            switch (i) {
//                case 2:
//                    if (isAllowableError(angles[i], mAngle)) {
//                        i = 0;
//                        angles[i] = mAngle;
//                        result = angles[i];
//                    }
//                    break;
//                default:
//                    if (isAllowableError(angles[i], mAngle)) {
//                        angles[++i] = mAngle;
//                        result = angles[i];
//                    }
//                    break;
//            }
//        }
    }

    private static boolean isAllowableError(double f1, double f2) {
        double error = f1 - f2;
        if (Math.abs(error) <= 20) {
            return true;
        } else {
            return false;
        }
    }

}
