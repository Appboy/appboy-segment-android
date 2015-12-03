package com.segment.analytics.android.integrations.appboy;

import android.test.AndroidTestCase;

import com.appboy.Appboy;
import com.segment.analytics.Analytics;
import com.segment.analytics.Traits;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.IdentifyPayloadBuilder;

import static com.segment.analytics.Utils.createTraits;

public class AppboyAndroidTest extends AndroidTestCase {
  public void testIdentifyCallsChangeUser() {
    String testUserId = "testUser" + System.currentTimeMillis();
    Traits traits = createTraits(testUserId);
    IdentifyPayload identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
    Logger logger = Logger.with(Analytics.LogLevel.DEBUG);
    AppboyIntegration integration = new AppboyIntegration(Appboy.getInstance(getContext()), "foo", logger);
    integration.identify(identifyPayload);

    assertEquals(testUserId, Appboy.getInstance(getContext()).getCurrentUser().getUserId());
  }
}
