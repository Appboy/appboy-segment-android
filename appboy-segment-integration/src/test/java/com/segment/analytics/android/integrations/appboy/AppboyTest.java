package com.segment.analytics.android.integrations.appboy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.Constants;
import com.appboy.enums.Gender;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;

import static com.segment.analytics.Utils.createTraits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

// Note - we can't use the Robelectric runner because it can't mock Appboy because it's final.
// This means that Android jar methods will return default values (because we configured it that
// way in the build.gradle) which can lead to strange behavior.
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "org.json.*",
    "com.appboy.Constants"})
@SuppressStaticInitializationFor("com.appboy.Constants")
@PrepareForTest({Appboy.class, AppboyInAppMessageManager.class, Constants.class})
public class AppboyTest  {
  @Mock Analytics mAnalytics;
  @Mock Application mContext;
  @Mock Appboy mAppboy;
  @Mock AppboyUser mAppboyUser;
  @Mock AppboyInAppMessageManager mInAppMessageManager;
  Logger mLogger;
  AppboyIntegration mIntegration;

  @Before
  public void setUp() {
    initMocks(this);
    mockStatic(Appboy.class);
    mockStatic(AppboyInAppMessageManager.class);
    when(Appboy.getInstance(any(Context.class))).thenReturn(mAppboy);
    when(mAppboy.getCurrentUser()).thenReturn(mAppboyUser);
    when(AppboyInAppMessageManager.getInstance()).thenReturn(mInAppMessageManager);
    mLogger = Logger.with(Analytics.LogLevel.DEBUG);
    when(mAnalytics.logger("Appboy")).thenReturn(mLogger);
    when(mAnalytics.getApplication()).thenReturn(mContext);
    mIntegration = new AppboyIntegration(mAppboy, "foo", mLogger, true);
  }

  @Test
  public void testFactoryLoadsProperValuesFromSettings() {
    mockStatic(Constants.class);
    ValueMap settings = new ValueMap().putValue("apiKey", "foo");
    AppboyIntegration integration = (AppboyIntegration) AppboyIntegration.FACTORY.create(settings, mAnalytics);
    assertThat(integration).isNotNull();
    assertThat(integration.getToken()).isEqualTo("foo");
  }

  @Test
  public void testActivityStartCallsOpenSession() {
    Activity activity = mock(Activity.class);
    mIntegration.onActivityStarted(activity);
    verify(mAppboy).openSession(activity);
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testActivityStopCallsCloseSession() {
    Activity activity = mock(Activity.class);
    mIntegration.onActivityStopped(activity);
    verify(mAppboy).closeSession(activity);
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testActivityResumeRegistersIamManager() {
    Activity activity = mock(Activity.class);
    mIntegration.onActivityResumed(activity);
    verifyStatic();
    AppboyInAppMessageManager.getInstance();
    verify(mInAppMessageManager).registerInAppMessageManager(activity);
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testActivityPauseUnregistersIamManager() {
    Activity activity = mock(Activity.class);
    mIntegration.onActivityPaused(activity);
    verifyStatic();
    AppboyInAppMessageManager.getInstance();
    verify(mInAppMessageManager).unregisterInAppMessageManager(activity);
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testIdentifyCallsChangeUser() {
    // The userId is stripped because Android's TextUtils is returning
    // a default length of 0.  Ported test to AppboyAndroidTest.java.
  }

  @Test
  public void testIdentifyCallWithNoFieldsSet() {
    Traits traits = createTraits("userId");
    IdentifyPayload identifyPayload = getBasicIdentifyPayloadWithTraits(traits);
    mIntegration.identify(identifyPayload);
    verify(mAppboy, Mockito.times(1)).changeUser("userId");
  }

  @Test
  public void testIdentifyFields() {
    Traits traits = createTraits("userId");
    traits.putEmail("a@o.o");
    traits.putFirstName("first");
    traits.putLastName("last");
    traits.putGender("male");
    traits.putPhone("5555551234");
    Traits.Address address = new Traits.Address();
    address.putCity("city");
    address.putCountry("country");
    traits.putAddress(address);
    traits.put("int", 10);
    traits.put("bool", Boolean.TRUE);
    traits.put("double", 4.2);
    traits.put("float", 5.0f);
    traits.put("long", 15L);
    traits.put("string", "value");
    traits.put("unknown", new Object());
    traits.put("userId", "id1");
    traits.put("anonymousId", "id2");
    IdentifyPayload identifyPayload = getBasicIdentifyPayloadWithTraits(traits);
    mIntegration.identify(identifyPayload);
    verify(mAppboyUser).setEmail("a@o.o");
    verify(mAppboyUser).setFirstName("first");
    verify(mAppboyUser).setLastName("last");
    verify(mAppboyUser).setGender(Gender.MALE);
    verify(mAppboyUser).setPhoneNumber("5555551234");
    verify(mAppboyUser).setHomeCity("city");
    verify(mAppboyUser).setCountry("country");
    verify(mAppboyUser).setCustomUserAttribute("int", 10);
    verify(mAppboyUser).setCustomUserAttribute("bool", Boolean.TRUE);
    verify(mAppboyUser).setCustomUserAttribute("double", 4.2);
    verify(mAppboyUser).setCustomUserAttribute("float", new Float(5.0));
    verify(mAppboyUser).setCustomUserAttribute("long", 15L);
    verify(mAppboyUser).setCustomUserAttribute("string", "value");
    verify(mAppboyUser, Mockito.never()).setCustomUserAttribute("userId", "id1");
    verify(mAppboyUser, Mockito.never()).setCustomUserAttribute("anonymousId", "id2");
    verify(mAppboy, Mockito.times(1)).changeUser("userId");
    verify(mAppboy, Mockito.times(1)).getCurrentUser();
  }

  @Test
  public void testIdentifyGender() {
    Traits traits = createTraits("userId");
    traits.putGender("male");
    IdentifyPayload identifyPayload = getBasicIdentifyPayloadWithTraits(traits);
    mIntegration.identify(identifyPayload);
    traits.putGender("MALe");
    identifyPayload = getBasicIdentifyPayloadWithTraits(traits);
    mIntegration.identify(identifyPayload);
    traits.putGender("m");
    identifyPayload = getBasicIdentifyPayloadWithTraits(traits);
    mIntegration.identify(identifyPayload);
    verify(mAppboyUser, Mockito.times(3)).setGender(Gender.MALE);
    traits.putGender("female");
    identifyPayload = getBasicIdentifyPayloadWithTraits(traits);
    mIntegration.identify(identifyPayload);
    traits.putGender("feMALe");
    identifyPayload = getBasicIdentifyPayloadWithTraits(traits);
    mIntegration.identify(identifyPayload);
    traits.putGender("f");
    identifyPayload = getBasicIdentifyPayloadWithTraits(traits);
    mIntegration.identify(identifyPayload);
    verify(mAppboy, Mockito.times(6)).changeUser("userId");
    verify(mAppboyUser, Mockito.times(3)).setGender(Gender.FEMALE);
    verify(mAppboyUser, Mockito.times(3)).setGender(Gender.MALE);
    verify(mAppboy, Mockito.times(6)).getCurrentUser();
  }

  @Test
  public void testIdentifyGenderOnBadInputs() {
    Traits traits = createTraits("userId");
    traits.putGender("males");
    IdentifyPayload identifyPayload = getBasicIdentifyPayloadWithTraits(traits);
    mIntegration.identify(identifyPayload);
    traits.putGender("female_1");
    identifyPayload = getBasicIdentifyPayloadWithTraits(traits);
    mIntegration.identify(identifyPayload);
    verify(mAppboy, Mockito.times(2)).changeUser("userId");
  }

  @Test
  public void testTrackLogsCustomEventWithoutProperties() {
    TrackPayload trackPayload = getBasicTrackPayloadWithEventAndProps("myEvent", null);
    mIntegration.track(trackPayload);

    // Note, testing event and purchase properties doesn't currently work because the toJsonObject
    // uses an Android method that returns a default value.
    // TODO - update tests once we've got a workaround.
    verify(mAppboy).logCustomEvent("myEvent");
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsCustomEventForNonOrderCompletedEventWithNoRevenueAndProducts() {
    Properties purchaseProperties = new Properties();
    purchaseProperties.putProducts(new Properties.Product("foo", "bar", 10));
    TrackPayload trackPayload = getBasicTrackPayloadWithEventAndProps("nonRevenueEvent", purchaseProperties);
    mIntegration.track(trackPayload);
    verify(mAppboy, Mockito.never()).logPurchase("c", "USD", new BigDecimal("10.0"));
    verify(mAppboy).logCustomEvent("nonRevenueEvent");
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsPurchaseForOrderCompletedEvent() {
    TrackPayload trackPayload = getBasicTrackPayloadWithEventAndProps("Order Completed", null);
    mIntegration.track(trackPayload);
    verify(mAppboy).logPurchase("Order Completed", "USD", new BigDecimal("0.0"));
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsPurchaseForEventWithRevenue() {
    Properties purchaseProperties = new Properties();
    purchaseProperties.putRevenue(10.0);
    TrackPayload trackPayload = getBasicTrackPayloadWithEventAndProps("revenueEvent", purchaseProperties);
    mIntegration.track(trackPayload);
    verify(mAppboy).logPurchase("revenueEvent", "USD", new BigDecimal("10.0"));
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsPurchasesForOrderCompletedEventWithProducts() {
    Properties purchaseProperties = new Properties();
    purchaseProperties.putProducts(new Properties.Product("id1", "sku1", 10), new Properties.Product("id2", "sku2", 12));
    TrackPayload trackPayload = getBasicTrackPayloadWithEventAndProps("Order Completed", purchaseProperties);
    mIntegration.track(trackPayload);
    verify(mAppboy).logPurchase("id1", "USD", new BigDecimal("10.0"));
    verify(mAppboy).logPurchase("id2", "USD", new BigDecimal("12.0"));
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsPurchasesForEventWithRevenueWithProducts() {
    Properties purchaseProperties = new Properties();
    purchaseProperties.putRevenue(10.0);
    purchaseProperties.putProducts(new Properties.Product("id1", "sku1", 10), new Properties.Product("id2", "sku2", 12));
    TrackPayload trackPayload = getBasicTrackPayloadWithEventAndProps("revenueEvent", purchaseProperties);
    mIntegration.track(trackPayload);
    verify(mAppboy).logPurchase("id1", "USD", new BigDecimal("10.0"));
    verify(mAppboy).logPurchase("id2", "USD", new BigDecimal("12.0"));
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsPurchaseForOrderCompletedEventWithCustomCurrency() {
    Properties purchaseProperties = new Properties();
    purchaseProperties.putCurrency("JPY");
    TrackPayload trackPayload = getBasicTrackPayloadWithEventAndProps("Order Completed", purchaseProperties);
    mIntegration.track(trackPayload);
    verify(mAppboy).logPurchase("Order Completed", "JPY", new BigDecimal("0.0"));
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsPurchaseForEventWithRevenueWithCustomCurrency() {
    Properties purchaseProperties = new Properties();
    purchaseProperties.putRevenue(10.0);
    purchaseProperties.putCurrency("JPY");
    TrackPayload trackPayload = getBasicTrackPayloadWithEventAndProps("revenueEvent", purchaseProperties);
    mIntegration.track(trackPayload);
    verify(mAppboy).logPurchase("revenueEvent", "JPY", new BigDecimal("10.0"));
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testScreenDoesNotCallAppboy() {
    mIntegration.screen(new ScreenPayload.Builder().userId("userId").name("foo").build());
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testFlushCallsRequestImmediateDataFlushOnce() {
    mIntegration.flush();
    verify(mAppboy).requestImmediateDataFlush();
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testResetHasNoInteractionWithAppboy() {
    mIntegration.reset();
    verifyNoMoreAppboyInteractions();
  }
  
  @Test
  public void whenPropertiesNull_logPurchaseForSingleItem_logsWithoutProperties() {
    final String productId = "id1";
    final String currencyCode = "USD";
    final BigDecimal price = new BigDecimal("1.00");
    mIntegration.logPurchaseForSingleItem(productId, currencyCode, price, null);
    verify(mAppboy).logPurchase(productId, currencyCode, price);
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void whenPropertiesEmpty_logPurchaseForSingleItem_logsWithoutProperties() {
    final String productId = "id1";
    final String currencyCode = "USD";
    final BigDecimal price = new BigDecimal("1.00");
    mIntegration.logPurchaseForSingleItem(productId, currencyCode, price, new JSONObject());
    verify(mAppboy).logPurchase(productId, currencyCode, price);
    verifyNoMoreAppboyInteractions();
  }

  private void verifyNoMoreAppboyInteractions() {
    verifyNoMoreInteractions(Appboy.class);
    verifyNoMoreInteractions(mAppboy);
  }

  private IdentifyPayload getBasicIdentifyPayloadWithTraits(Traits traits) {
    return new IdentifyPayload
      .Builder()
      .userId("userId")
      .traits(traits)
      .build();
  }

  private TrackPayload getBasicTrackPayloadWithEventAndProps(String event, Properties props) {
    if (props == null) {
      return new TrackPayload.Builder()
        .userId("userId")
        .event(event)
        .build();
    } else {
      return new TrackPayload.Builder()
        .userId("userId")
        .event(event)
        .properties(props)
        .build();
    }
  }
}
