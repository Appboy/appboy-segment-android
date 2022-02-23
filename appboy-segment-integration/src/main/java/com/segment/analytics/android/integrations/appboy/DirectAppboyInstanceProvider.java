package com.segment.analytics.android.integrations.appboy;

import com.appboy.IAppboy;

public class DirectAppboyInstanceProvider implements AppboyInstanceProvider {

  private IAppboy appboy;

  public DirectAppboyInstanceProvider(IAppboy appboy) {
    this.appboy = appboy;
  }

  @Override
  public IAppboy getInstance() {
    return appboy;
  }
}
