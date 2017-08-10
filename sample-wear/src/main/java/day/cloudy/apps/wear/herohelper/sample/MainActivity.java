package day.cloudy.apps.wear.herohelper.sample;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import day.cloudy.apps.wear.herohelper.HeroImageHelper;

public class MainActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mTextView;
    private GoogleApiClient mGoogleApiClient;
    private DataApi.DataListener mDataListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAmbientEnabled();
        setContentView(R.layout.activity_main);
        mTextView = findViewById(android.R.id.text1);
        mTextView.setText("Loading...");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (mDataListener != null)
                Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        // Get the local node for building the hero image data item uri
        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetLocalNodeResult getLocalNodeResult) {
                // Remove any existing hero image data item at the data item uri, so we're notified of future changes
                final Node localNode = getLocalNodeResult.getNode();
                final Uri uri = new Uri.Builder().scheme("wear").authority(localNode.getId()).path(HeroImageHelper.PATH_HERO_IMAGE).build();
                Wearable.DataApi.deleteDataItems(mGoogleApiClient, uri).setResultCallback(new ResultCallback<DataApi.DeleteDataItemsResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DeleteDataItemsResult deleteDataItemsResult) {
                        // Register a listener for  changes at the hero image data item uri
                        mDataListener = new MyDataListener();
                        Wearable.DataApi.addListener(mGoogleApiClient, mDataListener, uri, DataApi.FILTER_LITERAL);
                        // Put the hero image data item so that the companion can access it,
                        // no-op if the data item already exists so safe to call every app start
                        HeroImageHelper.putHeroImageDataItem(MainActivity.this);
                    }
                });
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
    }

    class MyDataListener implements DataApi.DataListener {

        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {
            for (int i = 0; i < dataEventBuffer.getCount(); i++) {
                DataEvent dataEvent = dataEventBuffer.get(i);
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                if (TextUtils.equals(uri.getPath(), HeroImageHelper.PATH_HERO_IMAGE)) {
                    mTextView.setText("Success");
                }
            }
        }
    }
}
