package com.segment.analytics.android.integrations.appboy;

import android.support.annotation.Nullable;

public class DefaultUserIdMapper implements UserIdMapper {

  @Override
  @Nullable
  public String transformUserId(@Nullable String segmentUserId) {
    return segmentUserId;
  }
}
