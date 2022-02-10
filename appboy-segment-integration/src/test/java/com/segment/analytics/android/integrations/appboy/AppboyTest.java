package com.segment.analytics.android.integrations.appboy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.appboy.IAppboy;
import com.appboy.enums.Gender;
import com.braze.models.outgoing.BrazeProperties;
import com.braze.BrazeUser;
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
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;

import java.util.ArrayList;
import java.math.BigDecimal;

import static com.segment.analytics.Utils.createTraits;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
@LooperMode(LooperMode.Mode.LEGACY)
@RunWith(RobolectricTestRunner.class)
public class AppboyTest {
  private IAppboy mAppboy;
  private BrazeUser mAppboyUser;
  private AppboyIntegration mIntegration;
  private Analytics mAnalytics;

  @Before
  public void setUp() {
    mAnalytics = mock(Analytics.class);
    final Application mockApplication = mock(Application.class);
    Mockito.when(mockApplication.getApplicationContext()).thenReturn(getContext());
    Mockito.when(mAnalytics.getApplication()).thenReturn(mockApplication);
    mAppboy = spy(new MockAppboy());
    mAppboyUser = mock(BrazeUser.class);

    when(mAppboy.getCurrentUser()).thenReturn(mAppboyUser);
    Logger logger = Logger.with(Analytics.LogLevel.DEBUG);
    when(mAnalytics.logger("Appboy")).thenReturn(logger);
    mIntegration = new AppboyIntegration(mAppboy, "foo", logger, true);
  }

  private Context getContext() {
    return ApplicationProvider.getApplicationContext();
  }

  @Test
  public void testFactoryLoadsProperValuesFromSettings() {
    ValueMap settings = new ValueMap().putValue("apiKey", "foo");
    AppboyIntegration integration = (AppboyIntegration) AppboyIntegration.FACTORY.create(settings, mAnalytics);
    assertNotNull(integration);
    assertEquals("foo", integration.getToken());
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
    final String[] testStringArray = {"Test Value 1", "Test Value 2"};
    traits.put("array", testStringArray);
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
    verify(mAppboyUser).setCustomAttributeArray("array", testStringArray);
    verify(mAppboyUser, Mockito.never()).setCustomUserAttribute("userId", "id1");
    verify(mAppboyUser, Mockito.never()).setCustomUserAttribute("anonymousId", "id2");
    verify(mAppboy, Mockito.times(1)).changeUser("userId");
    verify(mAppboy, Mockito.times(1)).getCurrentUser();
  }

  @Test
  public void testIdentifyArrayList() {
    Traits traits = createTraits("userId");
    ArrayList<String> testArrayList = new ArrayList<>();
    testArrayList.add("Test Value 1");
    testArrayList.add("Test Value 2");
    final String[] stringArray = {"Test Value 1", "Test Value 2"};
    final String testName = "testArrayList";
    traits.put(testName, testArrayList);
    IdentifyPayload identifyPayload = getBasicIdentifyPayloadWithTraits(traits);
    mIntegration.identify(identifyPayload);
    verify(mAppboyUser).setCustomAttributeArray(refEq(testName), aryEq(stringArray));
  }

  @Test
  public void testIdentifyArrayListWithListInt() {
    Traits traits = createTraits("userId");
    ArrayList<Integer> testArrayList = new ArrayList<>();
    testArrayList.add(1);
    testArrayList.add(2);
    final String[] stringArray = {"1", "2"};
    final String[] emptyArray = {};
    final String testName = "testArrayList";
    traits.put(testName, testArrayList);
    IdentifyPayload identifyPayload = getBasicIdentifyPayloadWithTraits(traits);
    mIntegration.identify(identifyPayload);
    verify(mAppboyUser, Mockito.never()).setCustomAttributeArray(refEq(testName), aryEq(stringArray));
    verify(mAppboyUser, Mockito.never()).setCustomAttributeArray(refEq(testName), aryEq(emptyArray));
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
    verify(mAppboy).logCustomEvent(Mockito.eq("nonRevenueEvent"), Mockito.any(BrazeProperties.class));
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
  public void testTrackLogsPurchaseForCompletedOrderEvent() {
    TrackPayload trackPayload = getBasicTrackPayloadWithEventAndProps("Completed Order", null);
    mIntegration.track(trackPayload);
    verify(mAppboy).logPurchase("Completed Order", "USD", new BigDecimal("0.0"));
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
    verify(mAppboy).logPurchase(Mockito.eq("id1"), Mockito.eq("USD"), Mockito.eq(new BigDecimal("10.0")), Mockito.any(BrazeProperties.class));
    verify(mAppboy).logPurchase(Mockito.eq("id2"), Mockito.eq("USD"), Mockito.eq(new BigDecimal("12.0")), Mockito.any(BrazeProperties.class));
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsPurchasesForCompletedOrderEventWithProducts() {
    Properties purchaseProperties = new Properties();
    purchaseProperties.putProducts(new Properties.Product("id1", "sku1", 10), new Properties.Product("id2", "sku2", 12));
    TrackPayload trackPayload = getBasicTrackPayloadWithEventAndProps("Completed Order", purchaseProperties);
    mIntegration.track(trackPayload);
    verify(mAppboy).logPurchase(Mockito.eq("id1"), Mockito.eq("USD"), Mockito.eq(new BigDecimal("10.0")), Mockito.any(BrazeProperties.class));
    verify(mAppboy).logPurchase(Mockito.eq("id2"), Mockito.eq("USD"), Mockito.eq(new BigDecimal("12.0")), Mockito.any(BrazeProperties.class));
    verifyNoMoreAppboyInteractions();
  }

  @Test
  public void testTrackLogsPurchasesForEventWithRevenueWithProducts() {
    Properties purchaseProperties = new Properties();
    purchaseProperties.putRevenue(10.0);
    purchaseProperties.putProducts(new Properties.Product("id1", "sku1", 10), new Properties.Product("id2", "sku2", 12));
    TrackPayload trackPayload = getBasicTrackPayloadWithEventAndProps("revenueEvent", purchaseProperties);
    mIntegration.track(trackPayload);
    verify(mAppboy).logPurchase(Mockito.eq("id1"), Mockito.eq("USD"), Mockito.eq(new BigDecimal("10.0")), Mockito.any(BrazeProperties.class));
    verify(mAppboy).logPurchase(Mockito.eq("id2"), Mockito.eq("USD"), Mockito.eq(new BigDecimal("12.0")), Mockito.any(BrazeProperties.class));
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
  public void testTrackLogsPurchaseForCompletedOrderEventWithCustomCurrency() {
    Properties purchaseProperties = new Properties();
    purchaseProperties.putCurrency("JPY");
    TrackPayload trackPayload = getBasicTrackPayloadWithEventAndProps("Completed Order", purchaseProperties);
    mIntegration.track(trackPayload);
    verify(mAppboy).logPurchase("Completed Order", "JPY", new BigDecimal("0.0"));
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
