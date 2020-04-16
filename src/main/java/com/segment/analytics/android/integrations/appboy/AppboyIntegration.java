package com.segment.analytics.android.integrations.appboy;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.configuration.AppboyConfig;
import com.appboy.enums.Gender;
import com.appboy.enums.Month;
import com.appboy.enums.SdkFlavor;
import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.models.outgoing.AttributionData;
import com.appboy.support.StringUtils;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.TrackPayload;
import com.segment.analytics.internal.Utils;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

public class AppboyIntegration extends Integration<Appboy> {
  private static final String APPBOY_KEY = "Appboy";
  private static final Set<String> MALE_TOKENS = new HashSet<String>(Arrays.asList("M",
      "MALE"));
  private static final Set<String> FEMALE_TOKENS = new HashSet<String>(Arrays.asList("F",
      "FEMALE"));
  private static final String DEFAULT_CURRENCY_CODE = "USD";
  private static final String API_KEY_KEY = "apiKey";
  private static final String CUSTOM_ENDPOINT_KEY = "customEndpoint";
  private static final String REVENUE_KEY = "revenue";
  private static final String CURRENCY_KEY = "currency";
  private static final String AUTOMATIC_IN_APP_MESSAGE_REGISTRATION_ENABLED =
      "automatic_in_app_message_registration_enabled";
  private static final List<String> RESERVED_KEYS = Arrays.asList("birthday", "email", "firstName",
      "lastName", "gender", "phone", "address", "avatar");

  public static final Factory FACTORY = factory(AppboyIntegrationOptions.builder().build());

  public static Factory factory(final AppboyIntegrationOptions options) {
    return new Factory() {
      @Override
      public Integration<?> create(ValueMap settings, Analytics analytics) {
        Logger logger = analytics.logger(APPBOY_KEY);
        String apiKey = settings.getString(API_KEY_KEY);
        SdkFlavor flavor = SdkFlavor.SEGMENT;
        boolean inAppMessageRegistrationEnabled =
            settings.getBoolean(AUTOMATIC_IN_APP_MESSAGE_REGISTRATION_ENABLED, true);

        if (StringUtils.isNullOrBlank(API_KEY_KEY)) {
          logger.info("Appboy+Segment integration attempted to initialize without api key.");
          return null;
        }

        String customEndpoint = settings.getString(CUSTOM_ENDPOINT_KEY);
        AppboyConfig.Builder builder = new AppboyConfig.Builder()
            .setApiKey(apiKey)
            .setSdkFlavor(flavor);
        if (!StringUtils.isNullOrBlank(customEndpoint)) {
          builder.setCustomEndpoint(customEndpoint);
        }

        Appboy.configure(analytics.getApplication().getApplicationContext(), builder.build());
        Appboy appboy = Appboy.getInstance(analytics.getApplication());
        logger.verbose("Configured Appboy+Segment integration and initialized Appboy.");
        return new AppboyIntegration(analytics.getApplication(), appboy, apiKey, logger, inAppMessageRegistrationEnabled,
            options.isTraitDiffingEnabled(), options.getUserIdMapper());
      }

      @Override
      public String key() {
        return APPBOY_KEY;
      }
    };
  }

  private final Appboy mAppboy;
  private final String mToken;
  private final Logger mLogger;
  private final boolean mAutomaticInAppMessageRegistrationEnabled;
  @NonNull
  private final UserIdMapper mUserIdMapper;
  @Nullable
  private final TraitsCache mTraitsCache;

  public AppboyIntegration(Context context, Appboy appboy, String token, Logger logger,
      boolean automaticInAppMessageRegistrationEnabled,
      boolean enableTraitDiffing,
      @Nullable UserIdMapper userIdMapper) {
    mAppboy = appboy;
    mToken = token;
    mLogger = logger;
    mAutomaticInAppMessageRegistrationEnabled = automaticInAppMessageRegistrationEnabled;
    mUserIdMapper = userIdMapper != null ? userIdMapper : new DefaultUserIdMapper();
    mTraitsCache = enableTraitDiffing ? new PreferencesTraitsCache(context) : null;
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
    String cachedUserId = mTraitsCache != null ? mTraitsCache.load().userId() : null;
    if (!StringUtils.isNullOrBlank(userId) && !userId.equals(cachedUserId)) {
      mLogger.debug("User ID changed. Old=" + cachedUserId + " New=" + userId);
      mAppboy.changeUser(mUserIdMapper.transformUserId(userId));

      if (mTraitsCache != null) {
        mTraitsCache.clear();
      }
    }

    AppboyUser currentUser = mAppboy.getCurrentUser();
    if (currentUser == null) {
      mLogger.info("Appboy.getCurrentUser() was null, aborting identify");
      return;
    }

    Traits originalTraits = identify.traits();
    Traits diffedTraits;
    if (mTraitsCache != null) {
      Traits lastEmittedTraits = mTraitsCache.load();
      diffedTraits = diffTraits(originalTraits, lastEmittedTraits);
    } else {
      diffedTraits = originalTraits;
    }

    Date birthday = diffedTraits.birthday();
    if (birthday != null) {
      Calendar birthdayCal = Calendar.getInstance(Locale.US);
      birthdayCal.setTime(birthday);
      currentUser.setDateOfBirth(birthdayCal.get(Calendar.YEAR),
          Month.values()[birthdayCal.get(Calendar.MONTH)],
          birthdayCal.get(Calendar.DAY_OF_MONTH));
    }

    String email = diffedTraits.email();
    if (!StringUtils.isNullOrBlank(email)) {
      currentUser.setEmail(email);
    }

    String firstName = diffedTraits.firstName();
    if (!StringUtils.isNullOrBlank(firstName)) {
      currentUser.setFirstName(firstName);
    }

    String lastName = diffedTraits.lastName();
    if (!StringUtils.isNullOrBlank(lastName)) {
      currentUser.setLastName(lastName);
    }

    String gender = diffedTraits.gender();
    if (!StringUtils.isNullOrBlank(gender)) {
      if (MALE_TOKENS.contains(gender.toUpperCase())) {
        currentUser.setGender(Gender.MALE);
      } else if (FEMALE_TOKENS.contains(gender.toUpperCase())) {
        currentUser.setGender(Gender.FEMALE);
      }
    }

    String phone = diffedTraits.phone();
    if (!StringUtils.isNullOrBlank(phone)) {
      currentUser.setPhoneNumber(phone);
    }

    Traits.Address address = diffedTraits.address();
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

    String avatarUrl = diffedTraits.avatar();
    if (!StringUtils.isNullOrBlank(avatarUrl)) {
      currentUser.setAvatarImageUrl(avatarUrl);
    }

    for (String key : diffedTraits.keySet()) {
      if (RESERVED_KEYS.contains(key)) {
        mLogger.debug("Skipping reserved key %s", key);
        continue;
      }
      Object value = diffedTraits.get(key);
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
      } else {
        mLogger.info("Appboy can't map segment value for custom Appboy user "
            + "attribute with key %s and value %s", key, value);
      }
    }

    if (mTraitsCache != null) {
      mTraitsCache.save(originalTraits);
    }
  }

  private Traits diffTraits(Traits traits, Traits lastEmittedTraits) {
    if (lastEmittedTraits == null) return traits;

    Traits diffed = new Traits();

    for (Map.Entry<String, Object> trait : traits.entrySet()) {
      Object storedValue = lastEmittedTraits.get(trait.getKey());

      if (storedValue != null) {
        boolean areEqual = trait.getValue().equals(storedValue) || haveSameNumberValue(trait.getValue(), storedValue);
        if (areEqual) {
          continue;
        }
      }

      diffed.put(trait.getKey(), trait.getValue());
    }

    return diffed;
  }

  private boolean haveSameNumberValue(Object o1, Object o2) {
    if (o1 instanceof Number && o2 instanceof Number) {
      Number n1 = (Number) o1;
      Number n2 = (Number) o2;
      return n1.doubleValue() == n2.doubleValue();
    } else {
      return false;
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
    if (event == null) {
      return;
    }
    Properties properties = track.properties();
    try {
      if (event.equals("Install Attributed")) {
        Properties campaignProps = (Properties) properties.get("campaign");
        if (campaignProps != null) {
          mAppboy.getCurrentUser().setAttributionData(new AttributionData(
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

    if (properties == null || properties.size() == 0) {
      mLogger.verbose("Calling appboy.logCustomEvent for event %s with no properties.", event);
      mAppboy.logCustomEvent(event);
      return;
    }

    if (event.equals("Order Completed") || properties.containsKey("revenue")) {
      String currency = StringUtils.isNullOrBlank(properties.currency())
          ? DEFAULT_CURRENCY_CODE
          : properties.currency();

      List<Properties.Product> products = properties.products();

      if (Utils.isNullOrEmpty(products)) {
        logPurchase(event, currency, properties);
      } else {
        for (Properties.Product product : products) {
          logProductPurchase(event, currency, product);
        }
      }

      return;
    }

    JSONObject propertiesJson = properties.toJsonObject();

    mLogger.verbose("Calling appboy.logCustomEvent for event %s with properties %s.",
        event, propertiesJson.toString());
    mAppboy.logCustomEvent(event, new AppboyProperties(propertiesJson));
  }

  private void logPurchase(String event, String currency, Properties properties) {
    double revenue = properties.revenue();

    if (revenue != 0) {
      JSONObject propertiesJson = properties.toJsonObject();

      propertiesJson.remove(REVENUE_KEY);
      propertiesJson.remove(CURRENCY_KEY);

      if (propertiesJson.length() == 0) {
        mLogger.verbose("Calling appboy.logPurchase for purchase %s for %.02f %s with no"
            + " properties.", event, revenue, currency);
        mAppboy.logPurchase(event, currency, new BigDecimal(revenue));
      } else {
        mLogger.verbose("Calling appboy.logPurchase for purchase %s for %.02f %s with properties"
            + " %s.", event, revenue, currency, propertiesJson.toString());
        mAppboy.logPurchase(event, currency, new BigDecimal(revenue),
            new AppboyProperties(propertiesJson));
      }
    }
  }

  private void logProductPurchase(String event, String currency, Properties.Product product) {
    String productId = product.id();
    BigDecimal price = new BigDecimal(product.price());

    AppboyProperties appboyProperties = new AppboyProperties();

    for (Map.Entry<String, Object> entry : product.entrySet()) {
      if (entry.getKey().equalsIgnoreCase("id")) continue;
      if (entry.getKey().equalsIgnoreCase("price")) continue;

      appboyProperties.addProperty(entry.getKey(), entry.getValue().toString());
    }

    if (appboyProperties.size() > 0) {
      mLogger.verbose("Calling appboy.logPurchase for purchase %s for %.02f %s with properties"
          + " %s.", event, price, currency, appboyProperties);
      mAppboy.logPurchase(productId, currency, price, appboyProperties);
    } else {
      mLogger.verbose("Calling appboy.logPurchase for purchase %s for %.02f %s with no"
          + " properties.", event, price, currency);
      mAppboy.logPurchase(productId, currency, price);
    }
  }

  @Override
  public void reset() {
    super.reset();

    if (mTraitsCache != null) {
      mTraitsCache.clear();
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
      AppboyInAppMessageManager.getInstance().registerInAppMessageManager(activity);
    }
  }

  @Override
  public void onActivityPaused(Activity activity) {
    super.onActivityPaused(activity);
    if (mAutomaticInAppMessageRegistrationEnabled) {
      AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(activity);
    }
  }
}
