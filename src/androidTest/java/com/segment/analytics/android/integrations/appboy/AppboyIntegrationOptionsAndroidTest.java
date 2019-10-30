package com.segment.analytics.android.integrations.appboy;

import android.support.test.runner.AndroidJUnit4;
import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.configuration.AppboyConfig;
import com.segment.analytics.Analytics;
import com.segment.analytics.Traits;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static android.support.test.InstrumentationRegistry.getContext;
import static com.segment.analytics.Utils.createTraits;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class AppboyIntegrationOptionsAndroidTest {

  private static final String USER_ID = "testUser";
  private static final String OTHER_USER_ID = "testUser2";
  private static final String TRANSFORMED_USER_ID = "transformedUser";
  private static final String TRAIT_EMAIL = "test@segment.com";
  private static final String TRAIT_EMAIL_UPDATED = "updated@segment.com";
  private static final String TRAIT_CITY = "city";
  private static final String TRAIT_COUNTRY = "country";
  private static final String CUSTOM_TRAIT_KEY = "custom_trait_key";
  private static final String CUSTOM_KEY_VALUE = "custom_key_value";

  @Mock Appboy appboy;
  @Mock AppboyUser appboyUser;

  private AppboyIntegration appboyIntegration;
  private PreferencesTraitsCache traitsCache;

  @BeforeClass
  public static void beforeClass() {
    AppboyConfig appboyConfig = new AppboyConfig.Builder().setApiKey("testkey").build();
    Appboy.configure(getContext(), appboyConfig);
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(appboy.getCurrentUser()).thenReturn(appboyUser);

    Logger logger = Logger.with(Analytics.LogLevel.DEBUG);

    traitsCache = new PreferencesTraitsCache(getContext());

    appboyIntegration = new AppboyIntegration(
        appboy, "foo", logger, true,
        traitsCache, new ReplaceUserIdMapper());
  }

  @After
  public void tearDown() throws Exception {
    traitsCache.clear();
  }

  @Test
  public void testUserIdMapperTransformsAppboyUserId() {
    Traits traits = createTraits(USER_ID);

    callIdentifyWithTraits(traits);

    verify(appboy).changeUser(TRANSFORMED_USER_ID);
  }

  @Test
  public void testShouldNotTriggerAppboyUpdateIfTraitDoesntChange() {
    Traits traits = createTraits(USER_ID);

    Traits.Address address = new Traits.Address();
    address.putCity(TRAIT_CITY);
    address.putCountry(TRAIT_COUNTRY);

    traits.putAddress(address);
    traits.putEmail(TRAIT_EMAIL);
    traits.put(CUSTOM_TRAIT_KEY, CUSTOM_KEY_VALUE);

    callIdentifyWithTraits(traits);
    callIdentifyWithTraits(traits);
    callIdentifyWithTraits(traits);

    verify(appboyUser, times(1)).setEmail(TRAIT_EMAIL);
    verify(appboyUser, times(1)).setHomeCity(TRAIT_CITY);
    verify(appboyUser, times(1)).setCountry(TRAIT_COUNTRY);
    verify(appboyUser, times(1)).setCustomUserAttribute(CUSTOM_TRAIT_KEY, CUSTOM_KEY_VALUE);
  }

  @Test
  public void testShouldTriggerUpdateIfTraitChanges() {
    Traits traits = createTraits(USER_ID);
    traits.putEmail(TRAIT_EMAIL);
    callIdentifyWithTraits(traits);
    callIdentifyWithTraits(traits);

    Traits traitsUpdate = createTraits(USER_ID);
    traitsUpdate.putEmail(TRAIT_EMAIL_UPDATED);
    callIdentifyWithTraits(traitsUpdate);
    callIdentifyWithTraits(traitsUpdate);

    InOrder inOrder = Mockito.inOrder(appboyUser);
    inOrder.verify(appboyUser, times(1)).setEmail(TRAIT_EMAIL);
    inOrder.verify(appboyUser, times(1)).setEmail(TRAIT_EMAIL_UPDATED);
  }

  @Test
  public void testAvoidTriggeringRepeatedUserIdUpdates() {
    Traits traits = createTraits(USER_ID);
    traits.putEmail(TRAIT_EMAIL);

    callIdentifyWithTraits(traits);
    callIdentifyWithTraits(traits);

    verify(appboyUser, times(1)).setEmail(TRAIT_EMAIL);
    verify(appboy, times(1)).changeUser(TRANSFORMED_USER_ID);
  }

  @Test
  public void clearCacheIfUserIdChanges() {
    Traits traits = createTraits(USER_ID);
    traits.putEmail(TRAIT_EMAIL);

    Traits traitsUpdate = createTraits(OTHER_USER_ID);
    traitsUpdate.putEmail(TRAIT_EMAIL);

    callIdentifyWithTraits(traits);
    callIdentifyWithTraits(traitsUpdate);

    verify(appboyUser, times(2)).setEmail(TRAIT_EMAIL);
  }

  @Test
  public void clearCacheOnReset() {
    Traits traits = createTraits(USER_ID);
    traits.putEmail(TRAIT_EMAIL);

    callIdentifyWithTraits(traits);

    appboyIntegration.reset();

    callIdentifyWithTraits(traits);

    verify(appboyUser, times(2)).setEmail(TRAIT_EMAIL);
  }

  public void callIdentifyWithTraits(Traits traits) {
    IdentifyPayload identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();

    appboyIntegration.identify(identifyPayload);
  }

  class ReplaceUserIdMapper implements UserIdMapper {
    @Override
    public String transformUserId(String segmentUserId) {
      return segmentUserId.replaceFirst(USER_ID, TRANSFORMED_USER_ID);
    }
  }
}
