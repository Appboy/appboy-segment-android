package com.segment.analytics.android.integrations.appboy;

import android.support.test.runner.AndroidJUnit4;

import com.appboy.Appboy;
import com.appboy.configuration.AppboyConfig;
import com.segment.analytics.Analytics;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Logger;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getContext;
import static com.segment.analytics.Utils.createTraits;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AppboyAndroidTest {

  @BeforeClass
  public static void beforeClass() {
    AppboyConfig appboyConfig = new AppboyConfig.Builder().setApiKey("testkey").build();
    Appboy.configure(getContext(), appboyConfig);
  }

  @Test
  public void testIdentifyCallsChangeUser() {
    String testUserId = "testUser" + System.currentTimeMillis();
    IdentifyPayload identifyPayload = new IdentifyPayload
      .Builder()
      .userId(testUserId)
      .traits(createTraits(testUserId))
      .build();
    AppboyIntegration integration = new AppboyIntegration(Appboy.getInstance(getContext()), "token", Logger.with(Analytics.LogLevel.DEBUG), true);

    integration.identify(identifyPayload);
    assertEquals(testUserId, Appboy.getInstance(getContext()).getCurrentUser().getUserId());
  }
}
