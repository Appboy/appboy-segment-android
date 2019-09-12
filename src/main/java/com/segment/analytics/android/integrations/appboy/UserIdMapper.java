package com.segment.analytics.android.integrations.appboy;

import com.appboy.Appboy;

public interface UserIdMapper {

  /**
   *
   * changeUser
   *
   * Returns the userId as should be reported to Braze to the {@link Appboy#changeUser(String)} method.
   *
   * @param segmentUserId
   * @return
   */
  String transformUserId(String segmentUserId);
}
