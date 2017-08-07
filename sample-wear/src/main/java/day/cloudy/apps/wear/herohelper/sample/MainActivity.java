package day.cloudy.apps.wear.herohelper.sample;

import android.app.Activity;
import android.os.Bundle;

import day.cloudy.apps.wear.herohelper.HeroImageHelper;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Put the hero image data so that the companion can access it,
        // no-op if the data item already exists so safe to call every app start
        HeroImageHelper.putHeroImageDataItem(this);

        setContentView(R.layout.activity_main);
    }
}
