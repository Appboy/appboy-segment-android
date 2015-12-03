package com.segment.analytics.android.integrations.appboy;

import android.app.Activity;

import com.appboy.Appboy;
import com.appboy.enums.Gender;
import com.appboy.enums.Month;
import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.ui.support.StringUtils;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class AppboyIntegration extends Integration<Appboy> {

  // TODO (Appboy) - Create configuration for default IAM handling
  private static final String APPBOY_KEY = "Appboy";
  private static final Set<String> MALE_TOKENS = new HashSet<String>(Arrays.asList("M",
      "MALE"));
  private static final Set<String> FEMALE_TOKENS = new HashSet<String>(Arrays.asList("F",
      "FEMALE"));
  private static final String DEFAULT_CURRENCY_CODE = "USD";
  private static final String API_KEY_KEY = "apiKey";
  private static final String REVENUE_KEY = "revenue";
  private static final String CURRENCY_KEY = "currency";
  private static final Set<String> APPBOY_RESERVED_TRAIT_KEYS = new HashSet<String>(Arrays.asList(
    "birthday", "email", "firstName", "lastName", "gender", "phone", "address", "anonymousId",
    "userId"));
  public static final Factory FACTORY = new Factory() {
    @Override
    public Integration<?> create(ValueMap settings, Analytics analytics) {
      Logger logger = analytics.logger(APPBOY_KEY);
      String apiKey = settings.getString(API_KEY_KEY);
      if (StringUtils.isNullOrBlank(API_KEY_KEY)) {
        logger.info("Appboy+Segment integration attempted to initialize without api key.");
        return null;
      }
      Appboy.configure(analytics.getApplication().getApplicationContext(), apiKey);
      Appboy appboy = Appboy.getInstance(analytics.getApplication());
      logger.verbose("Configured Appboy+Segment integration and initialized Appboy.");
      return new AppboyIntegration(appboy, apiKey, logger);
    }

    @Override
    public String key() {
      return APPBOY_KEY;
    }
  };

  private boolean mRefreshData;
  private final Appboy mAppboy;
  private final String mToken;
  private final Logger mLogger;

  public AppboyIntegration(Appboy appboy, String token, Logger logger) {
    mAppboy = appboy;
    mToken = token;
    mLogger = logger;
  }

  public String getToken() {
    return mToken;
  }

  @Override
  public Appboy getUnderlyingInstance() {
    return mAppboy;
  }

  @Override
  public void identify(IdentifyPayload identify) {
    super.identify(identify);

    String userId = identify.userId();
    if (!StringUtils.isNullOrBlank(userId)) {
      mAppboy.changeUser(identify.userId());
    }

    Traits traits = identify.traits();
    if (traits == null) {
      return;
    }

    // TODO (Appboy) - Uncomment once Segment resolves the crash caused by accessing bithday()
    Date birthday = null; // = traits.birthday();
    if (birthday != null) {
      mAppboy.getCurrentUser().setDateOfBirth(birthday.getYear(),
          Month.values()[birthday.getMonth()],
          birthday.getDay());
    }

    String email = traits.email();
    if (!StringUtils.isNullOrBlank(email)) {
      mAppboy.getCurrentUser().setEmail(email);
    }

    String firstName = traits.firstName();
    if (!StringUtils.isNullOrBlank(firstName)) {
      mAppboy.getCurrentUser().setFirstName(firstName);
    }

    String lastName = traits.lastName();
    if (!StringUtils.isNullOrBlank(lastName)) {
      mAppboy.getCurrentUser().setLastName(lastName);
    }

    String gender = traits.gender();
    if (!StringUtils.isNullOrBlank(gender)) {
      if (MALE_TOKENS.contains(gender.toUpperCase())) {
        mAppboy.getCurrentUser().setGender(Gender.MALE);
      } else if (FEMALE_TOKENS.contains(gender.toUpperCase())) {
        mAppboy.getCurrentUser().setGender(Gender.FEMALE);
      }
    }

    String phone = traits.phone();
    if (!StringUtils.isNullOrBlank(phone)) {
      mAppboy.getCurrentUser().setPhoneNumber(phone);
    }

    Traits.Address address = traits.address();
    if (address != null) {
      String city = address.city();
      if (!StringUtils.isNullOrBlank(city)) {
        mAppboy.getCurrentUser().setHomeCity(city);
      }
      String country = address.country();
      if (!StringUtils.isNullOrBlank(country)) {
        mAppboy.getCurrentUser().setCountry(country);
      }
    }

    for (String key : traits.keySet()) {
      if (!APPBOY_RESERVED_TRAIT_KEYS.contains(key)) {
        Object value = traits.get(key);
        if (value instanceof Boolean) {
          mAppboy.getCurrentUser().setCustomUserAttribute(key, (Boolean) value);
        } else if (value instanceof Integer) {
          mAppboy.getCurrentUser().setCustomUserAttribute(key, (Integer) value);
        } else if (value instanceof Float) {
          mAppboy.getCurrentUser().setCustomUserAttribute(key, (Float) value);
        } else if (value instanceof Long) {
          mAppboy.getCurrentUser().setCustomUserAttribute(key, (Long) value);
        } else if (value instanceof String) {
          mAppboy.getCurrentUser().setCustomUserAttribute(key, (String) value);
        } else {
          mLogger.info("Appboy can't map segment value for custom Appboy user "
            + "attribute with key %s and value %s", key, value);
        }
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
    String event = track.event();
    if (event == null) {
      return;
    }
    Properties properties = track.properties();
    if (properties == null || properties.size() == 0) {
      mLogger.verbose("Calling appboy.logCustomEvent for event %s with no properties.", event);
      mAppboy.logCustomEvent(event);
      return;
    }
    JSONObject propertiesJson = properties.toJsonObject();
    double revenue = properties.revenue();
    if (revenue != 0) {
      String currencyCode = StringUtils.isNullOrBlank(properties.currency()) ? DEFAULT_CURRENCY_CODE
        : properties.currency();
      propertiesJson.remove(REVENUE_KEY);
      propertiesJson.remove(CURRENCY_KEY);
      if (propertiesJson.length() == 0) {
        mLogger.verbose("Calling appboy.logPurchase for purchase %s for %.02f %s with no"
          + " properties.", event, revenue, currencyCode);
        mAppboy.logPurchase(event, currencyCode, new BigDecimal(revenue));
      } else {
        mLogger.verbose("Calling appboy.logPurchase for purchase %s for %.02f %s with properties"
            + " %s.", event, revenue, currencyCode, propertiesJson.toString());
        mAppboy.logPurchase(event, currencyCode, new BigDecimal(revenue),
          new AppboyProperties(propertiesJson));
      }
    } else {
      mLogger.verbose("Calling appboy.logCustomEvent for event %s with properties %s.",
        event, propertiesJson.toString());
      mAppboy.logCustomEvent(event, new AppboyProperties(propertiesJson));
    }
  }

  @Override
  public void onActivityStarted(Activity activity) {
    super.onActivityStarted(activity);
    if (mAppboy.openSession(activity)) {
      mRefreshData = true;
    }
  }

  @Override
  public void onActivityResumed(Activity activity) {
    super.onActivityResumed(activity);
    AppboyInAppMessageManager.getInstance().registerInAppMessageManager(activity);
    if (mRefreshData) {
      mAppboy.requestInAppMessageRefresh();
      mRefreshData = false;
    }
  }

  @Override
  public void onActivityPaused(Activity activity) {
    super.onActivityPaused(activity);
    AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(activity);
  }

  @Override
  public void onActivityStopped(Activity activity) {
    super.onActivityStopped(activity);
    mAppboy.closeSession(activity);
  }
}
