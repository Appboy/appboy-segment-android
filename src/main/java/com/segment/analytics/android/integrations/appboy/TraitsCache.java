package com.segment.analytics.android.integrations.appboy;

import com.segment.analytics.Traits;

interface TraitsCache {
  void save(Traits traits);

  Traits load();
}
