package day.cloudy.apps.wear.herohelper;

import android.content.Context;
import android.content.Intent;

import day.cloudy.apps.wear.herohelper.service.PutHeroDataItemService;

public class HeroImageHelper {

    public static final String PATH_HERO_IMAGE = "/day.cloudy.apps.wear.herohelper/hero-image";
    public static final String KEY_HERO_BITMAP = "hero_bitmap";

    public static void putHeroImageDataItem(Context context) {
        Intent intent = new Intent(context, PutHeroDataItemService.class);
        intent.setAction(PutHeroDataItemService.ACTION_PUT_HERO_IMAGE);
        context.startService(intent);
    }
}
