package day.cloudy.apps.wear.herohelper.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemAsset;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Map;

import day.cloudy.apps.wear.herohelper.HeroImageHelper;
import day.cloudy.apps.wear.herohelper.receiver.SetupDefaultsReceiver;

public class PutHeroDataItemService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = PutHeroDataItemService.class.getSimpleName();

    public static final String ACTION_PUT_HERO_IMAGE = "day.cloudy.apps.koko.action.PUT_HERO_IMAGE";

    private GoogleApiClient mGoogleApiClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        Log.d(TAG, "onHandleIntent: action = [" + action + "]");
        if (TextUtils.equals(action, ACTION_PUT_HERO_IMAGE)) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        putHeroDataItem();
    }

    @Override
    public void onConnectionSuspended(int reason) {
        Log.d(TAG, "onConnectionSuspended: reason = [" + reason + "]");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: error code = [" + connectionResult.getErrorCode() + "]");
    }

    private void putHeroDataItem() {
        Log.d(TAG, "putHeroDataItem: ");
        // get the local node to construct the data item uri
        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetLocalNodeResult localNodeResult) {
                Node localNode;
                if (!localNodeResult.getStatus().isSuccess() || (localNode = localNodeResult.getNode()) == null) {
                    Log.e(TAG, "onResult: error getting local node, " + localNodeResult.getStatus().getStatusMessage());
                    return;
                }
                // get any existing data item at this uri
                Uri uri = new Uri.Builder().scheme("wear").authority(localNode.getId()).path(HeroImageHelper.PATH_HERO_IMAGE).build();
                Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        if (!dataItemResult.getStatus().isSuccess()) {
                            Log.e(TAG, "onResult: error getting data item, " + dataItemResult.getStatus().getStatusMessage());
                            return;
                        }
                        // check if already put hero data item or not
                        DataItem dataItem = dataItemResult.getDataItem();
                        Map<String, DataItemAsset> assets;
                        if (dataItem != null && (assets = dataItem.getAssets()) != null && assets.containsKey(HeroImageHelper.KEY_HERO_BITMAP)) {
                            Log.d(TAG, "onResult: data item asset already exists, skip put");
                        } else {
                            Log.d(TAG, "onResult: data item asset does not exist, continue put");
                            // register a GET_SETUP_DEFAULTS receiver with lowest priority
                            IntentFilter filter = new IntentFilter(SetupDefaultsReceiver.ACTION_GET_SETUP_DEFAULTS);
                            filter.setPriority(IntentFilter.SYSTEM_LOW_PRIORITY);
                            registerReceiver(new SetupDefaultsReceiver(mGoogleApiClient) {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    // immediately unregister the GET_SETUP_DEFAULTS receiver
                                    unregisterReceiver(this);
                                    super.onReceive(context, intent);
                                }
                            }, filter);
                            // send the GET_SETUP_DEFAULTS ordered broadcast
                            Intent broadcastIntent = new Intent(SetupDefaultsReceiver.ACTION_GET_SETUP_DEFAULTS);
                            sendOrderedBroadcast(broadcastIntent, null);
                        }
                    }
                });
            }
        });
    }
}
