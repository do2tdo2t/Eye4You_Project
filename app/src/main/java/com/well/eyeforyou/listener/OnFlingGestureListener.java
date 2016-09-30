package com.well.eyeforyou.listener;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.well.eyeforyou.util.LogManager;

/**
 * Created by 박효정 on 2016-08-25.
 */
public class OnFlingGestureListener implements View.OnTouchListener {
    public GestureDetector gdt = null;
    private static final String TAG = "OnFlingGestureListenr";
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (gdt == null){
            gdt = new GestureDetector(v.getContext(), new GestureListener());
            LogManager.d(TAG, "gdt create.................");
        }
        return gdt.onTouchEvent(event);
    }

    public void onLeftToRight() {
    }

    public void onBottomToTop() {
    }


    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 150;
        private static final int SWIPE_THRESHOLD_VELOCITY = 100;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //Log.e(TAG,"onRightToLeft");
                //onRightToLeft();
                //return true;
            //} else
            if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Log.e(TAG,"onLeftToRight");
                onLeftToRight();
                return true;
            }

            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Log.e(TAG,"onBottomToTop");
                onBottomToTop();
                return true;
            } /*else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Log.e(TAG,"onTopToBottom");
                onTopToBottom();
                return true;
            }*/
            return false;
        }
    }
}
