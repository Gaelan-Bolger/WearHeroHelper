package day.cloudy.apps.wear.herohelper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;

import day.cloudy.apps.wear.herohelper.task.PutHeroDataItemTask;

public class SetupDefaultsReceiver extends BroadcastReceiver {

    private static final String TAG = SetupDefaultsReceiver.class.getSimpleName();

    public static final String ACTION_GET_SETUP_DEFAULTS = "com.google.android.wearable.action.GET_SETUP_DEFAULTS";
    public static final String KEY_PRODUCT_IMAGE_PACKAGE = "product_image_package";
    public static final String KEY_PRODUCT_IMAGE_RESID = "product_image_resid";

    private GoogleApiClient mGoogleApiClient;

    public SetupDefaultsReceiver(GoogleApiClient apiClient) {
        mGoogleApiClient = apiClient;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: action = [" + action + "]");
        if (TextUtils.equals(action, ACTION_GET_SETUP_DEFAULTS)) {
            Bundle extras = getResultExtras(true);
            String productImagePackage = extras.getString(KEY_PRODUCT_IMAGE_PACKAGE);
            int productImageResId = extras.getInt(KEY_PRODUCT_IMAGE_RESID, 0);
            putProductImage(context, productImagePackage, productImageResId);
        }
    }

    private void putProductImage(Context context, String productImagePackage, int productImageResId) {
        Log.d(TAG, "putProductImage: productImagePackage = [" + productImagePackage + "], productImageResId = [" + productImageResId + "]");
        if (TextUtils.isEmpty(productImagePackage) || productImageResId == 0) {
            Log.w(TAG, "Cannot put hero image, invalid hero image resource");
            return;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            Resources resources = packageManager.getResourcesForApplication(productImagePackage);
            Bitmap bitmap = BitmapFactory.decodeResource(resources, productImageResId);
            new PutHeroDataItemTask(mGoogleApiClient, new PutHeroDataItemTask.Callback() {
                @Override
                public void onResult(Integer statusCode) {
                    if (statusCode == CommonStatusCodes.SUCCESS)
                        Log.d(TAG, "onResult: Successfully put hero data item");
                    else
                        Log.e(TAG, "onResult: Error putting hero data item, status code = [" + statusCode + "]");
                }
            }).execute(bitmap);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error decoding hero image resource, " + e.getMessage());
        }
    }
}
