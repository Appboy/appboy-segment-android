package com.segment.analytics.android.integrations.appboy;

public class DefaultUserIdMapper implements UserIdMapper {

  @Override
  public String transformUserId(String segmentUserId) {
    return segmentUserId;
  }
}
