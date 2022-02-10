package com.segment.analytics.android.integrations.appboy;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import com.appboy.Appboy;
import com.appboy.IAppboy;
import com.appboy.enums.Gender;
import com.appboy.enums.Month;
import com.appboy.enums.SdkFlavor;
import com.braze.models.outgoing.BrazeProperties;
import com.appboy.models.outgoing.AttributionData;
import com.braze.Braze;
import com.braze.enums.BrazeSdkMetadata;
import com.braze.support.StringUtils;
import com.braze.BrazeUser;
import com.braze.configuration.BrazeConfig;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.TrackPayload;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AppboyIntegration extends Integration<Appboy> {
  private static final String APPBOY_KEY = "Appboy";
  private static final Set<String> MALE_TOKENS = new HashSet<>(Arrays.asList("M",
      "MALE"));
  private static final Set<String> FEMALE_TOKENS = new HashSet<>(Arrays.asList("F",
      "FEMALE"));
  private static final String DEFAULT_CURRENCY_CODE = "USD";
  private static final String API_KEY_KEY = "apiKey";
  private static final String CUSTOM_ENDPOINT_KEY = "customEndpoint";
  private static final String REVENUE_KEY = "revenue";
  private static final String CURRENCY_KEY = "currency";
  private static final String AUTOMATIC_IN_APP_MESSAGE_REGISTRATION_ENABLED =
          "automatic_in_app_message_registration_enabled";
  private static final List<String> RESERVED_KEYS = Arrays.asList("birthday", "email", "firstName",
    "lastName", "gender", "phone", "address", "anonymousId", "userId");

  public static final Factory FACTORY = new Factory() {
    @Override
    public Integration<?> create(ValueMap settings, Analytics analytics) {
      Logger logger = analytics.logger(APPBOY_KEY);
      String apiKey = settings.getString(API_KEY_KEY);
      SdkFlavor flavor = SdkFlavor.SEGMENT;
      boolean inAppMessageRegistrationEnabled =
              settings.getBoolean(AUTOMATIC_IN_APP_MESSAGE_REGISTRATION_ENABLED, true);

      if (StringUtils.isNullOrBlank(API_KEY_KEY)) {
        logger.info("Braze+Segment integration attempted to initialize without api key.");
        return null;
      }
      String customEndpoint = settings.getString(CUSTOM_ENDPOINT_KEY);
      BrazeConfig.Builder builder = new BrazeConfig.Builder()
          .setApiKey(apiKey)
          .setSdkFlavor(flavor)
          .setSdkMetadata(EnumSet.of(BrazeSdkMetadata.SEGMENT));
      if (!StringUtils.isNullOrBlank(customEndpoint)) {
        builder.setCustomEndpoint(customEndpoint);
      }

      final Context applicationContext = analytics.getApplication().getApplicationContext();
      Appboy.configure(applicationContext, builder.build());
      Appboy appboy = Appboy.getInstance(applicationContext);
      logger.verbose("Configured Braze+Segment integration and initialized Appboy.");
      return new AppboyIntegration(appboy, apiKey, logger, inAppMessageRegistrationEnabled);
    }

    @Override
    public String key() {
      return APPBOY_KEY;
    }
  };

  private final IAppboy mAppboy;
  private final String mToken;
  private final Logger mLogger;
  private final boolean mAutomaticInAppMessageRegistrationEnabled;

  public AppboyIntegration(Appboy appboy,
                           String token,
                           Logger logger,
                           boolean automaticInAppMessageRegistrationEnabled) {
    mAppboy = appboy;
    mToken = token;
    mLogger = logger;
    mAutomaticInAppMessageRegistrationEnabled = automaticInAppMessageRegistrationEnabled;
  }

  @RestrictTo(RestrictTo.Scope.TESTS)
  public AppboyIntegration(IAppboy appboy,
                           String token,
                           Logger logger,
                           boolean automaticInAppMessageRegistrationEnabled) {
    mAppboy = appboy;
    mToken = token;
    mLogger = logger;
    mAutomaticInAppMessageRegistrationEnabled = automaticInAppMessageRegistrationEnabled;
  }

  public String getToken() {
    return mToken;
  }

  @Override
  public Appboy getUnderlyingInstance() {
    return (Appboy) mAppboy;
  }

  @Override
  public void identify(IdentifyPayload identify) {
    super.identify(identify);

    String userId = identify.userId();
    if (!StringUtils.isNullOrBlank(userId)) {
      mAppboy.changeUser(identify.userId());
    }

    Traits traits = identify.traits();

    BrazeUser currentUser = mAppboy.getCurrentUser();
    if (currentUser == null) {
      mLogger.info("Appboy.getCurrentUser() was null, aborting identify");
      return;
    }

    Date birthday = traits.birthday();
    if (birthday != null) {
      Calendar birthdayCal = Calendar.getInstance(Locale.US);
      birthdayCal.setTime(birthday);
      currentUser.setDateOfBirth(birthdayCal.get(Calendar.YEAR),
          Month.values()[birthdayCal.get(Calendar.MONTH)],
          birthdayCal.get(Calendar.DAY_OF_MONTH));
    }

    String email = traits.email();
    if (!StringUtils.isNullOrBlank(email)) {
      currentUser.setEmail(email);
    }

    String firstName = traits.firstName();
    if (!StringUtils.isNullOrBlank(firstName)) {
      currentUser.setFirstName(firstName);
    }

    String lastName = traits.lastName();
    if (!StringUtils.isNullOrBlank(lastName)) {
      currentUser.setLastName(lastName);
    }

    String gender = traits.gender();
    if (!StringUtils.isNullOrBlank(gender)) {
      if (MALE_TOKENS.contains(gender.toUpperCase())) {
        currentUser.setGender(Gender.MALE);
      } else if (FEMALE_TOKENS.contains(gender.toUpperCase())) {
        currentUser.setGender(Gender.FEMALE);
      }
    }

    String phone = traits.phone();
    if (!StringUtils.isNullOrBlank(phone)) {
      currentUser.setPhoneNumber(phone);
    }

    Traits.Address address = traits.address();
    if (address != null) {
      String city = address.city();
      if (!StringUtils.isNullOrBlank(city)) {
        currentUser.setHomeCity(city);
      }
      String country = address.country();
      if (!StringUtils.isNullOrBlank(country)) {
        currentUser.setCountry(country);
      }
    }

    for (String key : traits.keySet()) {
      if (RESERVED_KEYS.contains(key)) {
        mLogger.debug("Skipping reserved key %s", key);
        continue;
      }
      Object value = traits.get(key);
      if (value instanceof Boolean) {
        currentUser.setCustomUserAttribute(key, (Boolean) value);
      } else if (value instanceof Integer) {
        currentUser.setCustomUserAttribute(key, (Integer) value);
      } else if (value instanceof Double) {
        currentUser.setCustomUserAttribute(key, (Double) value);
      } else if (value instanceof Float) {
        currentUser.setCustomUserAttribute(key, (Float) value);
      } else if (value instanceof Long) {
        currentUser.setCustomUserAttribute(key, (Long) value);
      } else if (value instanceof Date) {
        long secondsFromEpoch = ((Date) value).getTime() / 1000L;
        currentUser.setCustomUserAttributeToSecondsFromEpoch(key, secondsFromEpoch);
      } else if (value instanceof String) {
        currentUser.setCustomUserAttribute(key, (String) value);
      } else if (value instanceof String[]) {
        currentUser.setCustomAttributeArray(key, (String[]) value);
      } else if (value instanceof List<?>) {
        ArrayList<Object> valueArrayList = new ArrayList<Object>((Collection<Object>) value);
        List<String> stringValueList = new ArrayList<>();
        for (Object objectValue : valueArrayList) {
          if (objectValue instanceof String) {
            stringValueList.add((String) objectValue);
          }
        }
        if (stringValueList.size() > 0) {
          String[] arrayValue = new String[stringValueList.size()];
          currentUser.setCustomAttributeArray(key, stringValueList.toArray(arrayValue));
        }
      }
      else {
        mLogger.info("Braze can't map segment value for custom Braze user "
          + "attribute with key %s and value %s", key, value);
      }
    }
  }

  @Override
  public void flush() {
    super.flush();
    mLogger.verbose("Calling appboy.requestImmediateDataFlush().");
    mAppboy.requestImmediateDataFlush();
  }

  @Override
  public void track(TrackPayload track) {
    super.track(track);
    if (track == null) {
      return;
    }
    String event = track.event();
    Properties properties = track.properties();
    try {
      if (event.equals("Install Attributed")) {
        ValueMap campaignProps = (ValueMap) properties.get("campaign");
        BrazeUser currentUser = mAppboy.getCurrentUser();
        if (campaignProps != null && currentUser != null) {
          currentUser.setAttributionData(new AttributionData(
              campaignProps.getString("source"),
              campaignProps.getString("name"),
              campaignProps.getString("ad_group"),
              campaignProps.getString("ad_creative")));
        }
        return;
      }
    } catch (Exception exception) {
      mLogger.verbose("This Install Attributed event is not in the proper format and cannot be"
          + " logged. The exception is %s.", exception);
    }
    JSONObject propertiesJson = properties.toJsonObject();
    double revenue = properties.revenue();
    if (revenue != 0 || event.equals("Order Completed") || event.equals("Completed Order")) {
      String currencyCode = StringUtils.isNullOrBlank(properties.currency()) ? DEFAULT_CURRENCY_CODE
        : properties.currency();
      propertiesJson.remove(REVENUE_KEY);
      propertiesJson.remove(CURRENCY_KEY);

      if (properties.products() != null) {
        for (Properties.Product product : properties.products()) {
          logPurchaseForSingleItem(product.id(), currencyCode, BigDecimal.valueOf(product.price()), propertiesJson);
        }
      } else {
        logPurchaseForSingleItem(event, currencyCode, BigDecimal.valueOf(revenue), propertiesJson);
      }
    } else {
      if (propertiesJson == null || propertiesJson.length() == 0) {
        mLogger.verbose("Calling appboy.logCustomEvent for event %s", event);
        mAppboy.logCustomEvent(event);
      } else {
        mLogger.verbose("Calling appboy.logCustomEvent for event %s with properties %s.",
          event, propertiesJson.toString());
        mAppboy.logCustomEvent(event, new BrazeProperties(propertiesJson));
      }
    }
  }

  @Override
  public void onActivityStarted(Activity activity) {
    super.onActivityStarted(activity);
    mAppboy.openSession(activity);
  }

  @Override
  public void onActivityStopped(Activity activity) {
    super.onActivityStopped(activity);
    mAppboy.closeSession(activity);
  }

  @Override
  public void onActivityResumed(Activity activity) {
    super.onActivityResumed(activity);
    if (mAutomaticInAppMessageRegistrationEnabled) {
      BrazeInAppMessageManager.getInstance().registerInAppMessageManager(activity);
    }
  }

  @Override
  public void onActivityPaused(Activity activity) {
    super.onActivityPaused(activity);
    if (mAutomaticInAppMessageRegistrationEnabled) {
      BrazeInAppMessageManager.getInstance().unregisterInAppMessageManager(activity);
    }
  }

  @VisibleForTesting
  void logPurchaseForSingleItem(String productId,
                                        String currencyCode,
                                        BigDecimal price,
                                        @Nullable JSONObject propertiesJson) {
    if (propertiesJson == null || propertiesJson.length() == 0) {
      mLogger.verbose("Calling appboy.logPurchase for purchase %s for %.02f %s with no"
          + " properties.", productId, price, currencyCode);
      mAppboy.logPurchase(productId, currencyCode, price);
    } else {
      mLogger.verbose("Calling appboy.logPurchase for purchase %s for %.02f %s with properties"
          + " %s.", productId, price, currencyCode, propertiesJson.toString());
      mAppboy.logPurchase(productId, currencyCode, price, new BrazeProperties(propertiesJson));
    }
  }
}
