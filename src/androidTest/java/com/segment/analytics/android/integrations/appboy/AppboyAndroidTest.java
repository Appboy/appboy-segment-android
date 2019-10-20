package com.segment.analytics.android.integrations.appboy;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.appboy.Appboy;
import com.appboy.configuration.AppboyConfig;
import com.segment.analytics.Analytics;
import com.segment.analytics.Traits;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.segment.analytics.Utils.createTraits;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AppboyAndroidTest {

  @BeforeClass
  public static void beforeClass() {
    AppboyConfig appboyConfig = new AppboyConfig.Builder().setApiKey("testkey").build();
    Appboy.configure(getApplicationContext(), appboyConfig);
  }

  @Test
  public void testIdentifyCallsChangeUser() {
    String testUserId = "testUser" + System.currentTimeMillis();
    Traits traits = createTraits(testUserId);
    IdentifyPayload identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
    Logger logger = Logger.with(Analytics.LogLevel.DEBUG);
    AppboyIntegration integration = new AppboyIntegration(Appboy.getInstance(getApplicationContext()), "foo", logger, true);
    integration.identify(identifyPayload);

    assertEquals(testUserId, Appboy.getInstance(getApplicationContext()).getCurrentUser().getUserId());
  }
}
