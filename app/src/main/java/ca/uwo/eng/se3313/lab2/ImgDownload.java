package ca.uwo.eng.se3313.lab2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by darryl on 2016-10-13.
 */

public class ImgDownload implements IImageDownloader{
    @UiThread
    @Override
    public void download(@NonNull String imageUrl, @NonNull IImageDownloader.SuccessHandler handler) {
        class DownloadTask extends AsyncTask<String, Void, Bitmap> {

            @Override
            protected Bitmap doInBackground(String... params) {

                InputStream rawDownload;

                try {
                     rawDownload = (InputStream) new URL(params[0]).getContent();
                     return BitmapFactory.decodeStream(rawDownload);
                } catch(MalformedURLException e) {
                    throw new IllegalArgumentException(e.getMessage());
                } catch(Exception e) {
                    // TODO: Handle error with error handler. somehow.
                    throw new RuntimeException(e.getMessage());
                }
            }

            @Override
            protected void onPostExecute(Bitmap res) {
                handler.onComplete(res);
            }
        }

        new DownloadTask().execute(imageUrl);
    }
}
