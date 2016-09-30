package com.well.eyeforyou.caclulator;

/**
 * Created by user on 2016-09-21.
 */
public class AzimuthCaculator {
    private static float[] mOriVals = {0,0,0,0,0};
    private static float[] mAccMagVals={0,0,0,0,0};
    private static int i;
    private static int j;

    public AzimuthCaculator(){
        i = 0;
        j = 0;
    }

    public static float getCorrOrientationVal(float azimuth){
        float result = 0;
        if(mOriVals[0] == 0){
            mOriVals[0] = azimuth;
        }else{
            switch (i) {
                case 4:
                    if (isAllowableError(mOriVals[i], azimuth)) {
                        i = 0;
                        mOriVals[i] = azimuth;
                        result = mOriVals[i];
                    } else {
                        initialize(0);
                        i = 0;
                    }
                    break;
                default:
                    if (isAllowableError(mOriVals[i], azimuth)) {
                        mOriVals[++i] = azimuth;
                        result = mOriVals[i];
                    } else {
                        initialize(0);
                        i = 0;
                    }
                    break;
            }
        }
        if(mOriVals[0]*mOriVals[1]*mOriVals[2]*mOriVals[3]*mOriVals[4]==0) {
            result = 0;
        }
        return result;
    }

    public static float getCorrAccMagVal(float azimuth){
        float result = 0;
        if(mAccMagVals[0] == 0){
            mAccMagVals[0] = azimuth;
        }else{
            switch (j) {
                case 4:
                    if (isAllowableError(mAccMagVals[j], azimuth)) {
                        j = 0;
                        mAccMagVals[j] = azimuth;
                        result = mAccMagVals[j];
                    } else {
                        initialize(1);
                        j = 0;
                    }
                    break;
                default:
                    if (isAllowableError(mAccMagVals[j], azimuth)) {
                        mAccMagVals[++j] = azimuth;
                        result = mAccMagVals[j];
                    } else {
                        initialize(1);
                        j = 0;
                    }
                    break;
            }
        }
        if(mAccMagVals[0]*mAccMagVals[1]*mAccMagVals[2]*mAccMagVals[3]*mAccMagVals[4]==0) {
            result = 0;
        }
        return result;
    }

    private static boolean isAllowableError(float f1, float f2){
        float error = f1 - f2 ;
        if(Math.abs(error)<=10 ){
            return true;
        }else{
            return false;
        }
    }

    private static void initialize(int i){
        if(i == 0){
            mOriVals[0] = 0;
            mOriVals[1] = 0;
            mOriVals[2] = 0;
            mOriVals[3] = 0;
            mOriVals[4] = 0;

        }else if(i == 1){
            mAccMagVals[0] =0;
            mAccMagVals[1] =0;
            mAccMagVals[2] =0;
            mAccMagVals[3] =0;
            mAccMagVals[4] =0;
        }
    }

}
