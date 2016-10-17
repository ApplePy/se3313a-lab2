package ca.uwo.eng.se3313.lab2;

import android.os.CountDownTimer;
import android.os.Handler;

/**
 * The InfiniteCounter class uses the functionality of {@link CountDownTimer} to send a consistent
 * stream of continuous update ticks to the given message loop handler.
 *
 * @author Darryl Murray (dmurra47@uwo.ca)
 * @see CountDownTimer
 */
class InfiniteCounter extends CountDownTimer {
    /**
     * Message loop "what" identifier for Infinite counter ticks
     */
    static final int TIMER = 9001;
    private int onTickMillis;
    private Handler customHandler;
    // Workaround to beat "missing last tick" issue.
    private boolean tick = false;

    /**
     * Constructor for InfiniteCounter class.
     *
     * @param onTickMillis  The frequency (in milliseconds) of update ticks.
     * @param customHandler The handler attached to the message loop to receive the ticks.
     */
    InfiniteCounter(int onTickMillis, Handler customHandler) {
        super(onTickMillis * 60, onTickMillis / 2);
        this.onTickMillis = onTickMillis / 2;
        this.customHandler = customHandler;
    }

    /**
     * Runs when the counter ticks.
     *
     * @param millisUntilFinished Deprecated, has no effect.
     */
    @Override
    public void onTick(long millisUntilFinished) {
        // Workaround to beat "missing last tick" issue.
        tick = !tick;
        if (tick) {
            customHandler.sendEmptyMessage(TIMER);
        }
    }

    /**
     * Runs when the counter's loop has finished its run, and starts a new run.
     */
    @Override
    public void onFinish() {
        new InfiniteCounter(onTickMillis * 2, customHandler).start();
    }
}
