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
import com.segment.analytics.integrations.TrackPayload;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.ScreenPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import static com.segment.analytics.Utils.createTraits;

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
    mIntegration = new AppboyIntegration(
        mContext, mAppboy, "foo", mLogger, true, false, null);
  }

  @Test
  public void testFactoryLoadsProperValuesFromSettings() {
    mockStatic(Constants.class);
    ValueMap settings = new ValueMap().putValue("apiKey", "foo");
    AppboyIntegration integration = (AppboyIntegration) AppboyIntegration.FACTORY.create(settings, mAnalytics);
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
    IdentifyPayload identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
    mIntegration.identify(identifyPayload);
    verify(mAppboy, Mockito.times(1)).changeUser("userId");
    //verifyNoMoreAppboyUserInteractions();
    //verifyNoMoreAppboyInteractions();
  }

  @Test
 // @Ignore
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
    traits.putAvatar("avatarUrl");
    traits.put("int", new Integer(10));
    traits.put("bool", new Boolean(true));
    traits.put("double", new Double(4.2));
    traits.put("float", new Float(5.0));
    traits.put("long", new Long(15L));
    traits.put("string", "value");
    traits.put("unknown", new Object());

    IdentifyPayload identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
    mIntegration.identify(identifyPayload);
    verify(mAppboyUser).setEmail("a@o.o");
    verify(mAppboyUser).setFirstName("first");
    verify(mAppboyUser).setLastName("last");
    verify(mAppboyUser).setGender(Gender.MALE);
    verify(mAppboyUser).setPhoneNumber("5555551234");
    verify(mAppboyUser).setHomeCity("city");
    verify(mAppboyUser).setCountry("country");
    verify(mAppboyUser).setAvatarImageUrl("avatarUrl");
    verify(mAppboyUser).setCustomUserAttribute("int", new Integer(10));
    verify(mAppboyUser).setCustomUserAttribute("bool", new Boolean(true));
    verify(mAppboyUser).setCustomUserAttribute("double", new Double(4.2));
    verify(mAppboyUser).setCustomUserAttribute("float", new Float(5.0));
    verify(mAppboyUser).setCustomUserAttribute("long", new Long(15L));
    verify(mAppboyUser).setCustomUserAttribute("string", "value");
    //verifyNoMoreAppboyUserInteractions();
    verify(mAppboy, Mockito.times(1)).changeUser("userId");
    //verifyNoMoreAppboyInteractions();
  }

  @Test
  @Ignore
  public void testIdentifyGender() {
    Traits traits = createTraits("userId");
    traits.putGender("male");
    IdentifyPayload identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
    mIntegration.identify(identifyPayload);
    traits.putGender("MALe");
    identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
    mIntegration.identify(identifyPayload);
    traits.putGender("m");
    identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
    mIntegration.identify(identifyPayload);
    verify(mAppboyUser, Mockito.times(3)).setGender(Gender.MALE);
    traits.putGender("female");
    identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
    mIntegration.identify(identifyPayload);
    traits.putGender("feMALe");
    identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
    mIntegration.identify(identifyPayload);
    traits.putGender("f");
    identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
    mIntegration.identify(identifyPayload);
    verify(mAppboy, Mockito.times(6)).changeUser("userId");
    verify(mAppboyUser, Mockito.times(3)).setGender(Gender.FEMALE);
    //verifyNoMoreAppboyUserInteractions();
    verify(mAppboy, Mockito.times(6)).getCurrentUser();
    //verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testIdentifyGenderOnBadInputs() {
    Traits traits = createTraits("userId");
    traits.putGender("males");
    IdentifyPayload identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
    mIntegration.identify(identifyPayload);
    traits.putGender("female_1");
    identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
    mIntegration.identify(identifyPayload);
    verify(mAppboy, Mockito.times(2)).changeUser("userId");
    //verifyNoMoreAppboyUserInteractions();
    //verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsACustomEventWithoutProperties() {
    TrackPayload trackPayload = new TrackPayloadBuilder().event("myEvent").build();
    mIntegration.track(trackPayload);

    // Note, properties won't work in this test because the toJsonString uses an Android method
    // that returns a default value.
    // TODO - update tests once we've got a workaround.
    verify(mAppboy).logCustomEvent("myEvent");
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsAPurchaseWithoutProperties() {
    Properties purchaseProperties = new Properties();
    purchaseProperties.putRevenue(10.0);
    TrackPayload trackPayload = new TrackPayloadBuilder().event("myPurchase").properties(purchaseProperties).build();
    mIntegration.track(trackPayload);
    verify(mAppboy).logPurchase("myPurchase", "USD", new BigDecimal(10.0));
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsAPurchaseWithoutPropertiesWithCustomCurrency() {
    Properties purchaseProperties = new Properties();
    purchaseProperties.putRevenue(10.0);
    purchaseProperties.putCurrency("JPY");
    TrackPayload trackPayload = new TrackPayloadBuilder().event("myPurchase").properties(purchaseProperties).build();
    mIntegration.track(trackPayload);
    verify(mAppboy).logPurchase("myPurchase", "JPY", new BigDecimal(10.0));
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsAPurchaseForEachProduct() {
    Properties purchaseProperties = new Properties();
    purchaseProperties.putCurrency("EUR");
    purchaseProperties.putProducts(
        new Properties.Product("product_1", "sku.product.1", 10.99F),
        new Properties.Product("product_2", "sku.product.2", 20.99F)
    );

    TrackPayload trackPayload = new TrackPayloadBuilder().event("Order Completed").properties(purchaseProperties).build();

    mIntegration.track(trackPayload);

    verify(mAppboy).logPurchase("product_1", "EUR", new BigDecimal(10.99F));
    verify(mAppboy).logPurchase("product_2", "EUR", new BigDecimal(20.99F));
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testScreenDoesNotCallAppboy() {
    mIntegration.screen(new ScreenPayloadBuilder().name("foo").build());
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

  private void verifyNoMoreAppboyInteractions() {
    verifyNoMoreInteractions(Appboy.class);
    verifyNoMoreInteractions(mAppboy);
  }

  private void verifyNoMoreAppboyUserInteractions() {
    verifyNoMoreInteractions(AppboyUser.class);
    verifyNoMoreInteractions(mAppboyUser);
  }
}
