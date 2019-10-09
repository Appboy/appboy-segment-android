package com.segment.analytics.android.integrations.appboy;

import android.content.Context;
import android.content.SharedPreferences;
import com.segment.analytics.Cartographer;
import com.segment.analytics.Traits;
import java.io.IOException;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.segment.analytics.internal.Utils.isNullOrEmpty;

public class PreferencesTraitsCache implements TraitsCache {

  private static final String PREFS_FILENAME = "segment-braze-traits-cache";
  private static final String PREFS_KEY = "content";

  private final Cartographer cartographer;
  private final SharedPreferences preferences;

  public PreferencesTraitsCache(Context context) {
    preferences = context.getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE);
    cartographer = new Cartographer.Builder()
        .lenient(true)
        .prettyPrint(false)
        .build();
  }

  @Override
  public void save(Traits traits) {
    String json = cartographer.toJson(traits);
    preferences.edit().putString(PREFS_KEY, json).apply();
  }

  @Override
  public Traits load() {
    String json = preferences.getString(PREFS_KEY, null);

    if (isNullOrEmpty(json)) return null;

    try {
      Map<String, Object> map = cartographer.fromJson(json);
      return buildTraits(map);
    } catch (IOException ignored) {
      return null;
    }
  }

  @Override
  public void clear() {
    preferences.edit().clear().apply();
  }

  private Traits buildTraits(Map<String, Object> map) {
    Traits result = new Traits();

    for (Map.Entry<String, Object> entry: map.entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }

    return result;
  }
}
