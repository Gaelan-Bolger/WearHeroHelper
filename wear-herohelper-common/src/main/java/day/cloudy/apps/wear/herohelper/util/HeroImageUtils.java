package day.cloudy.apps.wear.herohelper.util;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.wearable.Asset;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class HeroImageUtils {

    public static File getHeroImageFile(Context context, String nodeId) {
        return new File(context.getFilesDir(), nodeId + "_hero.jpg");
    }

    /**
     * Builds an {@link com.google.android.gms.wearable.Asset} from a bitmap. The image that we get
     * back from the camera in "data" is a thumbnail size. Typically, your image should not exceed
     * 320x320 and if you want to have zoom and parallax effect in your app, limit the size of your
     * image to 640x400. Resize your image before transferring to your wearable device.
     */
    public static Asset toAsset(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        } finally {
            if (byteStream != null) {
                try {
                    byteStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
