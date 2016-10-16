package ca.uwo.eng.se3313.lab2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.view.KeyEvent.ACTION_DOWN;

public class MainActivity extends AppCompatActivity {

    /**
     * View that showcases the image
     */
    private ImageView ivDisplay;

    /**
     * Skip button
     */
    private ImageButton skipBtn;

    /**
     * Progress bar showing how many seconds left (percentage).
     */
    private ProgressBar pbTimeLeft;

    /**
     * Label showing the seconds left.
     */
    private TextView tvTimeLeft;

    /**
     * Control to change the interval between switching images.
     */
    private SeekBar sbWaitTime;

    /**
     * Editable text to change the interval with {@link #sbWaitTime}.
     */
    private EditText etWaitTime;


    /**
     * Used to download images from the {@link #urlList}.
     */
    private IImageDownloader imgDownloader;

    /**
     * List of image URLs of cute animals that will be displayed.
     */
    private static final List<String> urlList = new ArrayList<String>() {{
        add("http://i.imgur.com/CPqbVW8.jpg");
        add("http://i.imgur.com/Ckf5OeO.jpg");
        add("http://i.imgur.com/3jq1bv7.jpg");
        add("http://i.imgur.com/8bSITuc.jpg");
        add("http://i.imgur.com/JfKH8wd.jpg");
        add("http://i.imgur.com/KDfJruL.jpg");
        add("http://i.imgur.com/o6c6dVb.jpg");
        add("http://i.imgur.com/B1bUG2K.jpg");
        add("http://i.imgur.com/AfxvVuq.jpg");
        add("http://i.imgur.com/DSDtm.jpg");
        add("http://i.imgur.com/SAVYw7S.jpg");
        add("http://i.imgur.com/4HznKil.jpg");
        add("http://i.imgur.com/meeB00V.jpg");
        add("http://i.imgur.com/CPh0SRT.jpg");
        add("http://i.imgur.com/8niPBvE.jpg");
        add("http://i.imgur.com/dci41f3.jpg");
    }};

    private class TimeState {
        // The range of allowable timing values and current time (MainActivity owns the state)
        final int maxTime = 60;
        final int minTime = 5;
        private int currentTimeLeft = maxTime;
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
        @Deprecated
        private boolean setCurrentTimeLeft(int newTime) {
            synchronized (timeLeftLock) {
                synchronized (maxTimeLock) {
                    if (newTime >= 0 && newTime <= currentMaxTime) {
                        currentTimeLeft = newTime;
                        return true;
                    }
                    return false;
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

    TimeState timeState = new TimeState();

    // Message loop "what"
    final static int MAX_CHANGE = 9002;
    final static int CHANGE_IMAGE = 9003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Populate private members
        ivDisplay = (ImageView) findViewById(R.id.ivDisplay);
        skipBtn = (ImageButton) findViewById(R.id.btnSkip);
        pbTimeLeft = (ProgressBar) findViewById(R.id.pbTimeLeft);
        tvTimeLeft = (TextView) findViewById(R.id.tvTimeLeft);
        sbWaitTime = (SeekBar) findViewById(R.id.sbWaitTime);
        etWaitTime = (EditText) findViewById(R.id.etWaitTime);
        imgDownloader = new ImgDownload((@NonNull final Throwable error) -> {{
            Bitmap cat_error = BitmapFactory.decodeResource(this.getResources(), R.drawable.cat_error);
            ivDisplay.setImageBitmap(cat_error);
        }});


        // Initialize progress bar and edit text and slider to same value (60s)
        updateTimeCountdownUI(timeState.getCurrentMaxTime());
        updateTimingControls(timeState.getCurrentMaxTime());


        // Set up message loop
        Handler uiHandler = new Handler(Looper.getMainLooper()) {
            // Scary Java magic aside, stuff inside handleMessage is handled custom
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case InfiniteCounter.TIMER:
                        // Calculate new progress
                        Integer proposedNewTime = timeState.decrementTimeLeft();

                        // Update UI
                        if (proposedNewTime != null) {
                            updateTimeCountdownUI(proposedNewTime);
                        } else {
                            this.sendEmptyMessage(CHANGE_IMAGE);
                        }
                        break;
                    case MAX_CHANGE:  // Scroll bar or edit text changed
                        Log.d("MAX_CHANGE", "triggered");
                        timeState.setCurrentMaxTime((int)inputMessage.obj);
                        updateTimingControls(timeState.getCurrentMaxTime());
                        updateTimeCountdownUI(timeState.getCurrentTimeLeft());
                        break;
                    case CHANGE_IMAGE:
                        Log.d("CHANGE_IMAGE", "triggered");
                        imgDownloader.download(
                                urlList.get(new Random().nextInt(urlList.size())),
                                (Bitmap image) -> ivDisplay.setImageBitmap(image)
                        );
                        timeState.resetCurrentTimeLeft();
                        updateTimeCountdownUI(timeState.getCurrentMaxTime());
                        break;
                }
            }
        };

        // Add functionality to skip button
        skipBtn.setOnClickListener((View v) -> uiHandler.sendEmptyMessage(CHANGE_IMAGE));

        //Add functionality to the edit time
        etWaitTime.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction (TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_NULL && event.getAction() == ACTION_DOWN)
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    int newMax = Integer.parseInt(v.getText().toString());
                    if (newMax < timeState.minTime || newMax > timeState.maxTime) {
                        v.setError("Must specify number between 5 and 60.");
                    } else {
                        uiHandler.sendMessage(Message.obtain(uiHandler, MAX_CHANGE, newMax));

                        // Source: http://stackoverflow.com/questions/3553779/android-dismiss-keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
                return true;
            }
        });

        // Add ability to change time
        sbWaitTime.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTimingControls(progress + 5);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                uiHandler.sendMessage(Message.obtain(uiHandler, MAX_CHANGE, seekBar.getProgress() + 5));
            }
        });

        // Start with first image
        uiHandler.sendEmptyMessage(CHANGE_IMAGE);

        // Create timer to trigger countdowns
        new InfiniteCounter(timeState.maxTime * 1000, 1000, uiHandler).start();
    }

    @UiThread
    // Does this need to be synchronized? Only one UI thread though, so I should be good?
    private void updateTimeCountdownUI(int currentTimeLeft) {
        Integer progressPercent = 100 - currentTimeLeft * 100 / timeState.getCurrentMaxTime();
        pbTimeLeft.setProgress(progressPercent);
        tvTimeLeft.setText(
                Integer.valueOf(currentTimeLeft).toString().toCharArray(),
                0,
                Integer.valueOf(currentTimeLeft).toString().length()
        );  // LAZINESS
    }

    @UiThread
    // Does this need to be synchronized? Only one UI thread though, so I should be good?
    private void updateTimingControls (int currentMaxTime) {
        sbWaitTime.setProgress(currentMaxTime - 5);
        etWaitTime.setText(
                Integer.valueOf(currentMaxTime).toString().toCharArray(),
                0,
                Integer.valueOf(currentMaxTime).toString().length()
        );  // LAZINESS
    }
}

