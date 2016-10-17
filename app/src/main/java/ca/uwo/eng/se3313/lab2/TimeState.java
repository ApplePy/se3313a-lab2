package ca.uwo.eng.se3313.lab2;

import android.support.annotation.Nullable;

/**
 * Created by darryl on 2016-10-16.
 */

class TimeState {
    // The range of allowable timing values and current time (MainActivity owns the state)
    final int maxTime = 60;
    final int minTime = 5;
    private int currentTimeLeft = 50;
    private int currentMaxTime = maxTime;

    // Lock objects
    // PREVENT DEADLOCK! Grab currentTimeLock before MaxTime!
    private final Object timeLeftLock = new Object();
    private final Object maxTimeLock = new Object();


    int getCurrentTimeLeft() {synchronized (timeLeftLock) {return currentTimeLeft;}}
    int getCurrentMaxTime() {synchronized (maxTimeLock) {return currentMaxTime;}}
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
    int resetCurrentTimeLeft() {
        synchronized (timeLeftLock) {
            synchronized (maxTimeLock) {
                currentTimeLeft = currentMaxTime;
                return currentMaxTime;
            }
        }
    }
    void setCurrentMaxTime(int newMax) {
        synchronized (timeLeftLock) {
            synchronized (maxTimeLock) {
                if (newMax <= maxTime && newMax >= minTime) {
                    currentMaxTime = newMax;
                } else if (newMax > maxTime) {
                    currentMaxTime = maxTime;
                } else if (newMax < minTime) {
                    currentMaxTime = minTime;
                }

                if (currentTimeLeft > currentMaxTime) {
                    currentTimeLeft = currentMaxTime;
                }
            }
        }
    }
}
