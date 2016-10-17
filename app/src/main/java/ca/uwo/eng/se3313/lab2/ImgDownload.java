package ca.uwo.eng.se3313.lab2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by darryl on 2016-10-13.
 */

public class ImgDownload implements IImageDownloader{
    // Downsampling link to check on: https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    // Caching link to check on: https://developer.android.com/training/displaying-bitmaps/cache-bitmap.html

    private static final int TIMEOUT_SECS = 2;
    private static final int CACHE_SIZE_MiB = 6;
    private ErrorHandler errorHandler;
    private Throwable error;
    private LruCache<String, Bitmap> bitmapCache;

    @UiThread
    public ImgDownload(ErrorHandler handler) {
        errorHandler = handler;
        bitmapCache = new LruCache<String, Bitmap>(CACHE_SIZE_MiB * 1024 * 1024){
            @Override
            public void entryRemoved(boolean evicted,
                                     String key,
                                     Bitmap oldValue,
                                     Bitmap newValue)
            {
                oldValue.recycle();
            }
        };
    }

    @UiThread
    @Override
    public void download(@NonNull String imageUrl, @NonNull IImageDownloader.SuccessHandler handler) {
        class DownloadTask extends AsyncTask<URL, Void, Bitmap> {

            private SuccessHandler handler;

            private DownloadTask(SuccessHandler handler) {
                this.handler = handler;
            }

            @Override
            protected Bitmap doInBackground(URL... params) {

                InputStream rawDownload;

                try {
                    Log.d("Download", "starting");
                    // Source: http://stackoverflow.com/questions/5351689/alternative-to-java-net-url-for-custom-timeout-setting
                    HttpURLConnection connection = (HttpURLConnection) params[0].openConnection();
                    connection.setConnectTimeout(TIMEOUT_SECS * 1000);
                    connection.setReadTimeout(TIMEOUT_SECS * 1000);
                    rawDownload = connection.getInputStream();
                    Log.d("Download", "done");
                    return BitmapFactory.decodeStream(rawDownload);
                } catch(IOException e) {
                    Log.d("Download", "error");
                    error = e.getCause();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap res) {
                if (res != null) {
                    handler.onComplete(res);
                } else {
                    errorHandler.onError(error);
                }
            }
        }

        // Check cache first
        Bitmap cacheResult = bitmapCache.get(imageUrl);

        // If cache didn't have it
        if (cacheResult == null) {
            try {
                URL imageURL = new URL(imageUrl);
                Log.d("Download", "url created");
                SuccessHandler newHandler = (Bitmap v) -> {
                    bitmapCache.put(imageUrl, v);
                    handler.onComplete(v);
                };
                new DownloadTask(newHandler).execute(imageURL);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e.getMessage(), e.getCause());
            }
        } else {
            handler.onComplete(cacheResult);
        }
    }
}
