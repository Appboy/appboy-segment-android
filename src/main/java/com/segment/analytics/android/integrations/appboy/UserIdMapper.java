package com.segment.analytics.android.integrations.appboy;

import com.appboy.Appboy;

public interface UserIdMapper {

  /**
   * Defines a transformation for all userIds before being reported to the Braze SDK
   *
   * @param segmentUserId user id reported to segment in identify calls
   * @return userId as should be reported to Braze in the {@link Appboy#changeUser(String)} method.
   */
  String transformUserId(String segmentUserId);
}
