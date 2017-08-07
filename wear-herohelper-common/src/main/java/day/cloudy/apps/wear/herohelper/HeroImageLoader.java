package day.cloudy.apps.wear.herohelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemAsset;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.util.Map;

import day.cloudy.apps.wear.herohelper.task.GetBitmapFromAssetTask;
import day.cloudy.apps.wear.herohelper.util.HeroImageUtils;

import static day.cloudy.apps.wear.herohelper.HeroImageHelper.KEY_HERO_BITMAP;
import static day.cloudy.apps.wear.herohelper.HeroImageHelper.PATH_HERO_IMAGE;

public class HeroImageLoader {

    public static HeroImageLoader load(GoogleApiClient apiClient, String nodeId) {
        return new HeroImageLoader(apiClient, nodeId);
    }

    private static final String TAG = HeroImageLoader.class.getSimpleName();

    private final GoogleApiClient mApiClient;
    private final String mNodeId;
    private boolean mSkipCache;
    private Drawable mFallback;
    private ImageView mTarget;

    private HeroImageLoader(GoogleApiClient apiClient, String nodeId) {
        mApiClient = apiClient;
        mNodeId = nodeId;
    }

    public HeroImageLoader skipCache(boolean skipCache) {
        mSkipCache = skipCache;
        return this;
    }

    public HeroImageLoader fallback(Drawable fallback) {
        mFallback = fallback;
        return this;
    }

    public void into(ImageView imageView) {
        mTarget = imageView;
        loadHeroImage();
    }

    private void loadHeroImage() {
        if (TextUtils.isEmpty(mNodeId)) {
            Log.w(TAG, "loadHeroImage: node id must not be empty");
            return;
        }
        if (mTarget == null) {
            Log.w(TAG, "loadHeroImage: target view must not be null");
            return;
        }

        final File file = HeroImageUtils.getHeroImageFile(mTarget.getContext(), mNodeId);
        if (file.exists() && !mSkipCache) {
            Log.d(TAG, "loadHeroImage: load hero from cache");
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            loadImageView(mTarget, bitmap);
        } else {
            Log.d(TAG, "loadHeroImage: load hero from data item");
            Uri uri = new Uri.Builder().scheme("wear").authority(mNodeId).path(PATH_HERO_IMAGE).build();
            Wearable.DataApi.getDataItem(mApiClient, uri).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                    Log.d(TAG, "onResult: success = [" + dataItemResult.getStatus().isSuccess() + "]");
                    DataItem dataItem = dataItemResult.getDataItem();
                    if (dataItem == null) {
                        Log.d(TAG, "onResult: data item empty");
                        if (mFallback != null) loadImageView(mTarget, mFallback);
                    } else {
                        Log.d(TAG, "onResult: data item found");
                        Map<String, DataItemAsset> assets = dataItem.getAssets();
                        DataItemAsset dataItemAsset = assets.get(KEY_HERO_BITMAP);
                        Asset asset = Asset.createFromRef(dataItemAsset.getId());
                        new GetBitmapFromAssetTask(mApiClient, file, new GetBitmapFromAssetTask.Callback() {
                            @Override
                            public void onResult(@Nullable Bitmap bitmap) {
                                boolean success = bitmap != null;
                                Log.d(TAG, "onResult: success = [" + success + "]");
                                if (success) loadImageView(mTarget, bitmap);
                            }
                        }).execute(asset);
                    }
                }
            });
        }
    }

    private static void loadImageView(ImageView imageView, Bitmap bitmap) {
        loadImageView(imageView, new BitmapDrawable(imageView.getResources(), bitmap));
    }

    private static void loadImageView(ImageView imageView, Drawable drawable) {
        Drawable[] drawables = {imageView.getDrawable(), drawable};
        TransitionDrawable td = new TransitionDrawable(drawables);
        imageView.setImageDrawable(td);
        td.startTransition(300);
    }
}
