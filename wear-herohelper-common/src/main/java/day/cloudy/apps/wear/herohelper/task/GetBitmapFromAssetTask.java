package day.cloudy.apps.wear.herohelper.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GetBitmapFromAssetTask extends AsyncTask<Asset, Void, Bitmap> {

    public interface Callback {
        void onResult(@Nullable Bitmap bitmap);
    }

    private static final String TAG = GetBitmapFromAssetTask.class.getSimpleName();

    @NonNull
    private final GoogleApiClient mApiClient;
    @Nullable
    private final File mFile;
    @Nullable
    private final Callback mCallback;

    public GetBitmapFromAssetTask(@NonNull GoogleApiClient apiClient, @Nullable File file, @Nullable Callback callback) {
        mApiClient = apiClient;
        mFile = file;
        mCallback = callback;
    }

    @Override
    protected Bitmap doInBackground(Asset... assets) {
        if (!mApiClient.isConnected()) {
            Log.w(TAG, "GoogleApiClient not connected");
            return null;
        }
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mApiClient, assets[0]).await().getInputStream();
        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset");
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeStream(assetInputStream);
        if (bitmap != null && mFile != null) cacheHeroImage(bitmap, mFile);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mCallback != null)
            mCallback.onResult(bitmap);
        else if (bitmap != null)
            bitmap.recycle();
    }

    private void cacheHeroImage(@NonNull Bitmap bitmap, @NonNull File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Error caching hero image, " + e.getMessage());
        } finally {
            if (fos != null)
                try {
                    fos.close();
                } catch (IOException ignore) {
                }
        }
    }
}
