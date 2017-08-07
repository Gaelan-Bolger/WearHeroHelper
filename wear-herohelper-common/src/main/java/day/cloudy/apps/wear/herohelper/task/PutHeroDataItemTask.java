package day.cloudy.apps.wear.herohelper.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

import day.cloudy.apps.wear.herohelper.HeroImageHelper;
import day.cloudy.apps.wear.herohelper.util.HeroImageUtils;

public class PutHeroDataItemTask extends AsyncTask<Bitmap, Void, Integer> {

    public interface Callback {
        void onResult(Integer statusCode);
    }

    private static final String TAG = PutHeroDataItemTask.class.getSimpleName();

    @NonNull
    private final GoogleApiClient mApiClient;
    @Nullable
    private final Callback mCallback;

    public PutHeroDataItemTask(@NonNull GoogleApiClient apiClient, @Nullable Callback callback) {
        mApiClient = apiClient;
        mCallback = callback;
    }

    @Override
    protected Integer doInBackground(Bitmap... bitmaps) {
        if (!mApiClient.isConnected()) {
            Log.w(TAG, "GoogleApiClient not connected");
            return CommonStatusCodes.API_NOT_CONNECTED;
        }
        PutDataRequest request = PutDataRequest.create(HeroImageHelper.PATH_HERO_IMAGE);
        request.putAsset(HeroImageHelper.KEY_HERO_BITMAP, HeroImageUtils.toAsset(bitmaps[0]));
        DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mApiClient, request)
                .await(60 * 1000, TimeUnit.MILLISECONDS);
        return result.getStatus().getStatusCode();
    }

    @Override
    protected void onPostExecute(Integer statusCode) {
        if (mCallback != null) mCallback.onResult(statusCode);
    }
}