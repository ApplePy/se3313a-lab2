package ca.uwo.eng.se3313.lab2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *ImgDownload implements {@link IImageDownloader} to support downloading images from URLs.
 *
 * @author Darryl Murray (dmurra47@uwo.ca)
 * @version 1.0
 */
public class ImgDownload implements IImageDownloader{
    // Caching link to check on: https://developer.android.com/training/displaying-bitmaps/cache-bitmap.html

    private static final int TIMEOUT_SECS = 2;
    private static final int CACHE_SIZE_MiB = 6;
    private ErrorHandler errorHandler;
    private Throwable error;
    private LruCache<String, Bitmap> bitmapCache;

    /**
     * The constructor for ImgDownload
     *
     * @param handler The {@link ca.uwo.eng.se3313.lab2.IImageDownloader.ErrorHandler} to be used when downloads fail.
     */
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

    /**
     * Downloads a file asynchronously from a given link. Throws {@link IllegalArgumentException} if
     * the link is invalid.
     *
     * @param imageUrl String URL to download from.
     * @param handler  Code to execute in the UI thread on success (accepts a {@link Bitmap}).
     */
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
                    // Source: http://stackoverflow.com/questions/5351689/alternative-to-java-net-url-for-custom-timeout-setting
                    HttpURLConnection connection = (HttpURLConnection) params[0].openConnection();
                    connection.setConnectTimeout(TIMEOUT_SECS * 1000);
                    connection.setReadTimeout(TIMEOUT_SECS * 1000);
                    rawDownload = connection.getInputStream();
                    return BitmapFactory.decodeStream(rawDownload);
                } catch(IOException e) {
                    error = e.getCause();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap res) {
                if (res != null) {
                    handler.onComplete(res);
                } else {
                    Log.d("onDownload", "download error");
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
                Log.d("onDownload", "cache miss");
                SuccessHandler newHandler = (Bitmap v) -> {
                    bitmapCache.put(imageUrl, v);
                    handler.onComplete(v);
                };
                new DownloadTask(newHandler).execute(imageURL);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e.getMessage(), e.getCause());
            }
        } else {
            Log.d("OnDownload", "cache hit");
            handler.onComplete(cacheResult);
        }
    }
}
