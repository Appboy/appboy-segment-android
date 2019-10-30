package com.segment.analytics.android.integrations.appboy;

import com.segment.analytics.Traits;

public class InMemoryTraitsCache implements TraitsCache {

  private Traits traits = new Traits();

  @Override
  public void save(Traits traits) {
    this.traits = traits;
  }

  @Override
  public Traits load() {
    return traits;
  }

  @Override
  public void clear() {
    traits = new Traits();
  }
}
