package ca.uwo.eng.se3313.lab2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
public class ImgDownload implements IImageDownloader {
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
            /**
             * Removes cached bitmaps from memory when removed from the cache.
             *
             * @param evicted  true if the entry is being removed to make space, false if the removal was caused by a put(K, V) or remove(K).
             * @param key      key the entry was stored under.
             * @param oldValue the bitmap stored.
             * @param newValue the new value for key, if it exists. If non-null, this removal was caused by a put(K, V). Otherwise it was caused by an eviction or a remove(K).
             */
            @Override
            public void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
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
        /**
         * Asynchronous class that downloads images from given URLs using {@link AsyncTask}.
         *
         * @author Darryl Murray (dmurra47@uwo.ca)
         * @version 1.0
         */
        class DownloadTask extends AsyncTask<URL, Void, Bitmap> {
            private SuccessHandler handler;

            /**
             * The constructor for DownloadTask.
             *
             * @param handler The {@link ca.uwo.eng.se3313.lab2.IImageDownloader.SuccessHandler} to use on download success.
             */
            private DownloadTask(SuccessHandler handler) {
                this.handler = handler;
            }

            /**
             * Downloads new image in background.
             *
             * @param params The URL to download the image from.
             * @return The bitmap of the downloaded image.
             */
            @Override
            protected Bitmap doInBackground(URL... params) {

                InputStream rawDownload;

                try {
                    // Source: http://stackoverflow.com/questions/5351689/alternative-to-java-net-url-for-custom-timeout-setting
                    // Open connection to URL given
                    HttpURLConnection connection = (HttpURLConnection) params[0].openConnection();

                    // Set timeouts so the 404 cat appears eventually
                    connection.setConnectTimeout(TIMEOUT_SECS * 1000);
                    connection.setReadTimeout(TIMEOUT_SECS * 1000);

                    //Download image, convert into bitmap, and return it.
                    rawDownload = connection.getInputStream();
                    return BitmapFactory.decodeStream(rawDownload);
                } catch(IOException e) {
                    error = e.getCause();
                    return null;    // Return null to signify the download failed.
                }
            }

            /**
             * Run after download is complete. Runs the success or error handlers on completion/error.
             *
             * @param res The bitmap downloaded. Can be null.
             */
            @Override
            protected void onPostExecute(@Nullable Bitmap res) {
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

        // Download if cache didn't have it, otherwise return cached result.
        if (cacheResult == null) {
            try {
                // Create URL object, modify the handler to use in download task to add caching, and run task.
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
