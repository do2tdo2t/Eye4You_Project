package com.well.eyeforyou.component;

/**
 * Created by 박효정 on 2016-07-05.
 */
public class KalmanFilter {
    private float Q = 0.00001f;
    private float R = 0.001f;
    private float X = 0, P = 1, K;

    public KalmanFilter(float initValue) {
        X = initValue;
    }

    private void measurementUpdate(){
        K = (P + Q) / (P + Q + R);
        P = R * (P + Q) / (R + P + Q);
    }

    public float update(float measurement){
        measurementUpdate();
        X = X + (measurement - X) * K;

        return X;
    }

}
