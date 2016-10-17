package ca.uwo.eng.se3313.lab2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
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
import java.util.Locale;
import java.util.Random;

import static android.view.KeyEvent.ACTION_DOWN;

public class MainActivity extends AppCompatActivity {

    /**
     * Message loop "what" constant, specifying that the countdown max changed. Send new countdown as int in Message obj.
     */
    public final static int MAX_CHANGE = 9002;
    /**
     * Message loop "what" constant, specifying that the image expired.
     */
    public final static int CHANGE_IMAGE = 9003;
    /**
     * Message loop "what" constant, specifying that a new image is available. Send new image as Bitmap in Message obj.
     */
    public final static int IMAGE_AVAILABLE = 9004;
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
     * Contains the current state of countdown to next image switch.
     */
    private TimeState timeState = new TimeState();

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
        // imgDownloader initialized below after uiHandler



        // Set up message loop
        Handler uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            /**
             * This method handles the custom messages generated in this application.
             * @param inputMessage The message that is sent.
             */
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    // Triggered when the timer goes off to update the UI
                    case InfiniteCounter.TIMER:
                        // Update progress
                        Integer proposedNewTime = timeState.decrementTimeLeft();

                        // Update UI
                        if (proposedNewTime != null)
                            updateTimeCountdownUI(proposedNewTime);
                        else
                            this.sendEmptyMessage(CHANGE_IMAGE);
                        break;
                    // Triggered when scroll bar or EditText changed in a way that other elements need updating
                    // Note: message obj contains the new max countdown time
                    case MAX_CHANGE:
                        timeState.setCurrentMaxTime((int)inputMessage.obj);     // Update max time with passed number
                        updateTimingControls(timeState.getCurrentMaxTime());    // Update other timing controls
                        updateTimeCountdownUI(timeState.getCurrentTimeLeft());  // Update countdown UI
                        break;
                    // Triggered when the countdown expires and the image needs to be update
                    case CHANGE_IMAGE:
                        // Pick a new random image and download it, sending a message when the image is ready
                        imgDownloader.download(
                                urlList.get(new Random().nextInt(urlList.size())),
                                (Bitmap image) -> this.sendMessage(
                                        Message.obtain(
                                                this,
                                                IMAGE_AVAILABLE,
                                                image))
                        );
                        timeState.resetCurrentTimeLeft();                       // Reset countdown
                        updateTimeCountdownUI(timeState.getCurrentMaxTime());   // Update countdown UI
                        break;
                    // Triggered when an image download has completed and a new image is available.
                    // Note: message obj contains the new image
                    case IMAGE_AVAILABLE:
                        Bitmap photo = (Bitmap) inputMessage.obj;   // Get image
                        ivDisplay.setImageBitmap(photo);            // Display image
                        break;
                }
            }
        };




        // Set up message passing to/from imgDownloader
        imgDownloader = new ImgDownload((@NonNull final Throwable error) -> {
            // Get cat_error picture (leave it to GC to cleanup)
            Bitmap cat_error = BitmapFactory.decodeResource(this.getResources(), R.drawable.cat_error);
            // Send cat_error picture for display
            uiHandler.sendMessage(Message.obtain(uiHandler, IMAGE_AVAILABLE, cat_error));
        });



        // Initialize progress bar and edit text and slider to same value (60s)
        updateTimeCountdownUI(timeState.getCurrentMaxTime());
        updateTimingControls(timeState.getCurrentMaxTime());



        // Set skip button to trigger CHANGE_IMAGE messages
        skipBtn.setOnClickListener((View v) -> uiHandler.sendEmptyMessage(CHANGE_IMAGE));

        //Add functionality to edit the time with typing in durations directly.
        etWaitTime.setSelectAllOnFocus(false);  // Android doesn't always make up it's mind, do it manually
        etWaitTime.setOnClickListener((View v) -> ((EditText) v).selectAll());      // Select all text when clicked on
        etWaitTime.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) ->  // Update when "Enter" or "Done" are pressed
        {
            // If the enter key was pressed down or if the "done" button was pressed
            if ((actionId == EditorInfo.IME_NULL && event.getAction() == ACTION_DOWN)
                    || actionId == EditorInfo.IME_ACTION_DONE)
            {

                Integer newMax = null;
                try {
                    newMax = Integer.parseInt(v.getText().toString());  // Get the new time
                } catch (NumberFormatException e) {
                    newMax = 0; // Handles non-numerical input
                }

                // If the number is invalid, specify error
                if (newMax < timeState.minTime || newMax > timeState.maxTime) {
                    v.setError("Must specify number between 5 and 60.");
                } else {
                    // Hide the keyboard
                    // Source: http://stackoverflow.com/questions/3553779/android-dismiss-keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    // Signal that a change to max countdown happened and it's new value
                    uiHandler.sendMessage(Message.obtain(uiHandler, MAX_CHANGE, newMax));
                }
            }
            return true;
        });

        // Add ability to change time
        sbWaitTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            /**
             * Triggered when the seek bar value changed, but hasn't stopped changing.
             * @param seekBar The UI object being changed.
             * @param progress The current value of the seekBar.
             * @param fromUser 'True' if the user is causing the change. 'False' otherwise.
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the timing controls to display the proposed changes as it happens
                updateTimingControls(progress + 5);
            }

            /**
             * Triggered when the seek bar is starting to be changed.
             * @param seekBar The UI object being changed.
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            /**
             * Triggered when the seek bar is finished changing.
             * @param seekBar The UI object being changed.
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // User settled on a new max countdown, send message so it takes effect
                uiHandler.sendMessage(Message.obtain(uiHandler, MAX_CHANGE, seekBar.getProgress() + 5));
            }
        });



        // Start with first image
        uiHandler.sendEmptyMessage(CHANGE_IMAGE);



        // Create continuous timer to trigger countdowns (ticks once a second)
        new InfiniteCounter(1000, uiHandler).start();
    }


    @UiThread
    /**
     * This function updates the countdown portion of the UI with the latest current time left
     * from the TimeState.
     * @param currentTimeLeft The time left to be displayed on the UI.
     */
    private void updateTimeCountdownUI(int currentTimeLeft)
    {
        // Convert the current time left into a percentage left
        Integer progressPercent = 100 - currentTimeLeft * 100 / timeState.getCurrentMaxTime();
        pbTimeLeft.setProgress(progressPercent);                                    // Update the progress bar
        tvTimeLeft.setText(String.format(Locale.CANADA, "%d", currentTimeLeft));    // Update text view
    }


    @UiThread
    /**
     * This function updates the editing countdown portion of the UI with the latest current max
     * time from the TimeState.
     * @param currentMaxTime The current max countdown time to be displayed on the UI.
     */
    private void updateTimingControls (int currentMaxTime) {
        sbWaitTime.setProgress(currentMaxTime - 5);                                 // Update the seek bar
        etWaitTime.setText(String.format(Locale.CANADA, "%d", currentMaxTime));     // Update the edit text
    }
}

