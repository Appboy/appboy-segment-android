package com.segment.analytics.android.integrations.appboy;

import android.support.test.runner.AndroidJUnit4;
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

import static android.support.test.InstrumentationRegistry.getContext;
import static com.segment.analytics.Utils.createTraits;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AppboyIntegrationOptionsAndroidTest {

  private static final String ORIGINAL_USER_ID = "testUser";
  private static final String TRANSFORMED_USER_ID = "transformedUser";

  @BeforeClass
  public static void beforeClass() {
    AppboyConfig appboyConfig = new AppboyConfig.Builder().setApiKey("testkey").build();
    Appboy.configure(getContext(), appboyConfig);
  }

  @Test
  public void testUserIdMapperTransformsAppboyUserId() {
    Traits traits = createTraits(ORIGINAL_USER_ID);
    IdentifyPayload identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();

    Logger logger = Logger.with(Analytics.LogLevel.DEBUG);

    AppboyIntegration integration = new AppboyIntegration(Appboy.getInstance(getContext()), "foo", logger, true, new ReplaceUserIdMapper());

    integration.identify(identifyPayload);

    assertEquals(TRANSFORMED_USER_ID, Appboy.getInstance(getContext()).getCurrentUser().getUserId());
  }

  class ReplaceUserIdMapper implements UserIdMapper {
    @Override
    public String transformUserId(String segmentUserId) {
      return segmentUserId.replaceFirst(ORIGINAL_USER_ID, TRANSFORMED_USER_ID);
    }
  }
}

