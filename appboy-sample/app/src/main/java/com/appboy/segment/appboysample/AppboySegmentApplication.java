package com.appboy.segment.appboysample;

import android.app.Application;
import android.util.Log;

import com.braze.support.BrazeLogger;
import com.segment.analytics.Analytics;
import com.segment.analytics.android.integrations.appboy.AppboyIntegration;

public class AppboySegmentApplication extends Application {
  private static final String TAG = String.format("%s.%s", BrazeLogger.getBrazeLogTag(AppboySegmentApplication.class), AppboySegmentApplication.class.getName());
  private static final String WRITE_KEY = @"YOUR_WRITE_KEY";
  private static final String APPBOY_KEY = "Appboy";
  public static boolean sAppboySegmentEnabled = false;

  @Override public void onCreate() {
    super.onCreate();
    BrazeLogger.setLogLevel(Log.VERBOSE);
    Analytics.Builder builder = new Analytics.Builder(this, WRITE_KEY);
    builder.use(AppboyIntegration.FACTORY);
    builder.logLevel(Analytics.LogLevel.VERBOSE);
    Analytics.setSingletonInstance(builder.build());

    Analytics.with(this).onIntegrationReady(APPBOY_KEY, new Analytics.Callback() {
      @Override
      public void onReady(Object instance) {
        Log.i(TAG, "analytics.onIntegrationReady() called");
        sAppboySegmentEnabled = true;
      }
    });
  }
}
