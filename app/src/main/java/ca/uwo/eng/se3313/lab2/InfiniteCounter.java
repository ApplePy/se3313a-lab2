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

    // HAX! Workaround to beat "missing last tick" issue.
    boolean tick = false;

    InfiniteCounter(int finishMillis, int onTickMillis, Handler customHandler) {
        super(finishMillis, onTickMillis / 2);
        this.finishMillis = finishMillis;
        this.onTickMillis = onTickMillis / 2;
        this.customHandler = customHandler;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        Log.d("onTick", Long.toString(millisUntilFinished));
        // HAX! Workaround to beat "missing last tick" issue.
        tick = !tick;
        if (tick) {
            Log.d ("onTick", "TOCK");
            customHandler.sendEmptyMessage(TIMER);
        }
    }

    @Override
    public void onFinish() {
        Log.d("onFinish", "Finished.");
        new InfiniteCounter(finishMillis, onTickMillis * 2, customHandler).start();
    }
}
