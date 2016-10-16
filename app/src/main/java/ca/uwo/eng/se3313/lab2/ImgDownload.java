package ca.uwo.eng.se3313.lab2;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by darryl on 2016-10-13.
 */

public class ImgDownload implements IImageDownloader{
    private Bitmap cat_error;

    @UiThread
    public ImgDownload(Context appContext) {
        Resources temp = appContext.getResources();
        cat_error =  BitmapFactory.decodeResource(temp, R.drawable.cat_error);
    }

    @UiThread
    @Override
    public void download(@NonNull String imageUrl, @NonNull IImageDownloader.SuccessHandler handler) {
        class DownloadTask extends AsyncTask<URL, Void, Bitmap> {

            @Override
            protected Bitmap doInBackground(URL... params) {

                InputStream rawDownload;

                try {
                    Log.d("Download", "starting");
                    // Source: http://stackoverflow.com/questions/5351689/alternative-to-java-net-url-for-custom-timeout-setting
                    HttpURLConnection connection = (HttpURLConnection) params[0].openConnection();
                    connection.setConnectTimeout(5 * 1000);
                    connection.setReadTimeout(5 * 1000);
                    rawDownload = connection.getInputStream();
                    Log.d("Download", "done");
                    return BitmapFactory.decodeStream(rawDownload);
                } catch(IOException e) {
                    Log.d("Download", "error");
                    return cat_error;
                }
            }

            @Override
            protected void onPostExecute(Bitmap res) {
                handler.onComplete(res);
            }
        }

        try {
            URL imageURL = new URL(imageUrl);
            Log.d("Download", "url created");
            new DownloadTask().execute(imageURL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage(), e.getCause());
        }
    }
}
