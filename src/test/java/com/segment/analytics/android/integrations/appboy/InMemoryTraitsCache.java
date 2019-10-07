package com.segment.analytics.android.integrations.appboy;

import com.segment.analytics.Traits;

public class InMemoryTraitsCache implements TraitsCache {

  private Traits traits;

  @Override
  public void save(Traits traits) {
    this.traits = traits;
  }

  @Override
  public Traits load() {
    return traits;
  }
}
