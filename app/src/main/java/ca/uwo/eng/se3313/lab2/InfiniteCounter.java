package ca.uwo.eng.se3313.lab2;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

/**
 * Created by darryl on 2016-10-14.
 */

class InfiniteCounter extends CountDownTimer {
    private int finishMillis;
    private int onTickMillis;
    private Handler customHandler;
    static final int TIMER = 9001;

    InfiniteCounter(int finishMillis, int onTickMillis, Handler customHandler) {
        super(finishMillis, onTickMillis);
        this.finishMillis = finishMillis;
        this.onTickMillis = onTickMillis;
        this.customHandler = customHandler;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        Log.d("onTick", Long.toString(millisUntilFinished));
        customHandler.sendEmptyMessage(TIMER);


    }

    @Override
    public void onFinish() {
        Log.d("onFinish", "Finished.");
        new InfiniteCounter(finishMillis, onTickMillis, customHandler).start();
    }
}
