package ca.uwo.eng.se3313.lab2;

import android.support.annotation.Nullable;

/**
 * TimeState maintains the current state of the app countdown timer in a thread-safe manner.
 *
 * @author Darryl Murray (dmurra47@uwo.ca)
 * @version 1.0
 */
class TimeState {
    /**
     * The maximum allowable max time value.
     */
    final int maxTime = 60;

    /**
     * The minimum allowable max time value.
     */
    final int minTime = 5;
    // Lock objects
    // PREVENT DEADLOCK! Grab currentTimeLock before MaxTime!
    private final Object timeLeftLock = new Object();
    private final Object maxTimeLock = new Object();
    private int currentTimeLeft = 50;
    private int currentMaxTime = maxTime;

    /**
     * Returns the amount of time left in the counter.
     *
     * @return Returns the amount of time left in the counter.
     */
    int getCurrentTimeLeft() {synchronized (timeLeftLock) {return currentTimeLeft;}}

    /**
     * Returns the current maximum time allowed in the counter.
     *
     * @return Returns the current maximum time allowed in the counter.
     */
    int getCurrentMaxTime() {synchronized (maxTimeLock) {return currentMaxTime;}}

    /**
     * Changes the value that the counter resets to on completion, to up max specified by maxTime.
     *
     * @param newMax The value to be used as the counter's new max time.
     */
    void setCurrentMaxTime(int newMax) {
        synchronized (timeLeftLock) {
            synchronized (maxTimeLock) {

                // Make sure received value is within limits.
                if (newMax <= maxTime && newMax >= minTime)
                    currentMaxTime = newMax;
                else
                    throw new IllegalArgumentException("The value supplied is outside the allowable range.");

                // "Fast-forward" current time if too high.
                if (currentTimeLeft > currentMaxTime) {
                    currentTimeLeft = currentMaxTime;
                }
            }
        }
    }

    /**
     * Decrements the time left in the counter by one.
     *
     * @return Returns the time left in the counter after the decrement, or null if the counter has reached zero.
     */
    @Nullable
    Integer decrementTimeLeft() {
        synchronized (timeLeftLock) {
            if (currentTimeLeft - 1 >= 0) {
                return --currentTimeLeft;
            } else {
                return null;
            }
        }
    }

    /**
     * Resets the counter to the time specified by current max time.
     *
     * @return Returns the time the counter was reset to.
     */
    int resetCurrentTimeLeft() {
        synchronized (timeLeftLock) {
            synchronized (maxTimeLock) {
                currentTimeLeft = currentMaxTime;
                return currentMaxTime;
            }
        }
    }
}
