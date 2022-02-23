package com.segment.analytics.android.integrations.appboy;

import android.content.Context;
import com.appboy.Appboy;

public class FromContextAppboyInstanceProvider implements AppboyInstanceProvider {

  private final Context mContext;

  public FromContextAppboyInstanceProvider(Context context) {
    mContext = context;
  }

  @Override
  public Appboy getInstance() {
    return Appboy.getInstance(mContext);
  }
}
