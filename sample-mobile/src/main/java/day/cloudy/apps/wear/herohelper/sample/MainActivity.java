package day.cloudy.apps.wear.herohelper.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import day.cloudy.apps.wear.herohelper.HeroImageLoader;
import day.cloudy.apps.wear.herohelper.service.HeroImageListenerService;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageView mHeroImage;

    private GoogleApiClient mGoogleApiClient;
    private ConnectedNodesAdapter mAdapter;

    private BroadcastReceiver mHeroCacheChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: action = [" + intent.getAction() + "]");
            String nodeId = intent.getStringExtra(HeroImageListenerService.EXTRA_NODE_ID);
            Node selectedNode = mAdapter.getSelectedItem();
            if (selectedNode != null && TextUtils.equals(selectedNode.getId(), nodeId))
                loadHeroImage(selectedNode);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ConnectedNodesAdapter(this);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHeroImage = findViewById(R.id.hero);

        RecyclerView nodesRecycler = findViewById(R.id.recycler_view);
        nodesRecycler.setHasFixedSize(true);
        nodesRecycler.setLayoutManager(new LinearLayoutManager(this));
        nodesRecycler.setAdapter(mAdapter);

        ItemClickSupport ics = ItemClickSupport.addTo(nodesRecycler);
        ics.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                loadHeroImage(mAdapter.getItem(position));
                mAdapter.setSelection(position);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(HeroImageListenerService.ACTION_HERO_CACHE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mHeroCacheChangedReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mHeroCacheChangedReceiver);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        if (getConnectedNodesResult.getStatus().isSuccess()) {
                            List<Node> nodes = getConnectedNodesResult.getNodes();
                            loadHeroImage(nodes.get(0));
                            mAdapter.setItems(nodes, 0);
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int reason) {
        Log.w(TAG, "onConnectionSuspended: reason = [" + reason + "]");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: error code = [" + connectionResult.getErrorCode() + "]");
    }

    private void loadHeroImage(Node node) {
        Drawable fallback = ContextCompat.getDrawable(this, R.drawable.default_hero);
        HeroImageLoader.load(mGoogleApiClient, node.getId()).fallback(fallback).into(mHeroImage);
    }
}
