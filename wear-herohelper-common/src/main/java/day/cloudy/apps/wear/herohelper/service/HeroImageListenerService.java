package day.cloudy.apps.wear.herohelper.service;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemAsset;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.util.Map;

import day.cloudy.apps.wear.herohelper.HeroImageHelper;
import day.cloudy.apps.wear.herohelper.task.GetBitmapFromAssetTask;
import day.cloudy.apps.wear.herohelper.util.HeroImageUtils;

/**
 * {@link WearableListenerService} used on the companion device to receive hero item data item changes.
 */
public class HeroImageListenerService extends WearableListenerService {

    private static final String TAG = HeroImageListenerService.class.getSimpleName();

    public static final String ACTION_HERO_CACHE_CHANGED = "day.cloudy.apps.wear.herohelper.action.HERO_CACHE_CHANGED";
    public static final String EXTRA_NODE_ID = "node_id";

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        boolean success = dataEventBuffer.getStatus().isSuccess();
        Log.d(TAG, "onDataChanged: success = [" + success + "]");
        int count;
        if (success && (count = dataEventBuffer.getCount()) > 0) {
            for (int i = 0; i < count; i++) {
                DataEvent event = dataEventBuffer.get(i);
                DataItem dataItem = event.getDataItem();
                if (dataItem != null) {
                    final String nodeId = dataItem.getUri().getAuthority();
                    final Map<String, DataItemAsset> assets = dataItem.getAssets();
                    if (assets != null && assets.containsKey(HeroImageHelper.KEY_HERO_BITMAP)) {
                        mGoogleApiClient = new GoogleApiClient.Builder(this)
                                .addApi(Wearable.API).build();
                        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect();
                        if (connectionResult.isSuccess()) {
                            DataItemAsset dataItemAsset = assets.get(HeroImageHelper.KEY_HERO_BITMAP);
                            Asset asset = Asset.createFromRef(dataItemAsset.getId());
                            File file = HeroImageUtils.getHeroImageFile(HeroImageListenerService.this, nodeId);
                            new GetBitmapFromAssetTask(mGoogleApiClient, file, new GetBitmapFromAssetTask.Callback() {
                                @Override
                                public void onResult(@Nullable Bitmap bitmap) {
                                    boolean success = bitmap != null;
                                    Log.d(TAG, "onResult: success = [" + success + "]");
                                    if (success) {
                                        sendHeroCacheChangedBroadcast(nodeId);
                                        bitmap.recycle();
                                    }
                                    if (mGoogleApiClient.isConnected())
                                        mGoogleApiClient.disconnect();
                                }
                            }).execute(asset);
                        } else {
                            Log.e(TAG, "Error connecting GoogleApiClient, error code = {" + connectionResult.getErrorCode() + "]");
                        }
                    } else {
                        Log.w(TAG, "No bitmap asset contained in data item");
                    }
                } else {
                    Log.w(TAG, "No data item contained in data event");
                }
            }
        } else {
            Log.w(TAG, "Invalid data event buffer");
        }
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    private void sendHeroCacheChangedBroadcast(String nodeId) {
        Log.d(TAG, "sendHeroCacheChangedBroadcast: ");
        Intent intent = new Intent(ACTION_HERO_CACHE_CHANGED);
        intent.putExtra(EXTRA_NODE_ID, nodeId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
