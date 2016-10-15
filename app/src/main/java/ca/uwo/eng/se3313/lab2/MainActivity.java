package ca.uwo.eng.se3313.lab2;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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

    // The range of allowable timing values
    private final static int maxTime = 60;
    private final static int minTime = 5;
    private static int currentTimeLeft = 5;
    private static int currentMaxTime = 5;

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
        imgDownloader = new ImgDownload();


        // Initialize progress bar and edit text and slider to same value (60s)
        ResetTimingUI();

        // Set up message loop
        Handler uiHandler = new Handler(Looper.getMainLooper()) {
            final int CHANGE_IMAGE = 9003;

            // Scary Java magic aside, stuff inside handleMessage is handled custom
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case InfiniteCounter.TIMER:
                        // Calculate new progress
                        //currentTimeLeft = Integer.parseInt(tvTimeLeft.getText().toString()) - 1;

                        // Update UI
                        if (currentTimeLeft >= 0) {
                            //UpdateTimingUI();
                        } else {
                            //this.sendEmptyMessage(CHANGE_IMAGE);
                        }
                        break;
                    case 9002:
                        // TODO: Implement scrollbar changed value
                        break;
                    case 9003:
                        //ResetTimingUI();
                        // TODO: Download cat, reset timer, update image.
                }
            }
        };

        // Add ability to change time
        sbWaitTime.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                uiHandler.sendEmptyMessage(9002);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Create timer to trigger countdowns
        new InfiniteCounter(5 * 1000, 1000, uiHandler).start();

    }

    @UiThread
    private void UpdateTimingUI() {
        Integer progressPercent = currentTimeLeft * 100 / currentMaxTime;
        pbTimeLeft.setProgress(progressPercent);
        etWaitTime.setText(
                Integer.valueOf(currentTimeLeft).toString().toCharArray(),
                0,
                Integer.valueOf(currentTimeLeft).toString().length()
        );  // LAZINESS
        tvTimeLeft.setText(
                Integer.valueOf(currentTimeLeft).toString().toCharArray(),
                0,
                Integer.valueOf(currentTimeLeft).toString().length()
        );  // LAZINESS
    }

    @UiThread
    private void ResetTimingUI() {
        sbWaitTime.setProgress(100);
        currentTimeLeft = currentMaxTime;
        UpdateTimingUI();
    }
}

