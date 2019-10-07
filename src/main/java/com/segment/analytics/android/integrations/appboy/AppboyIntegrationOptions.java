package com.segment.analytics.android.integrations.appboy;

public class AppboyIntegrationOptions {

  private UserIdMapper userIdMapper;

  public static Builder builder() {
    return new Builder();
  }

  UserIdMapper getUserIdMapper() {
    return userIdMapper;
  }

  private AppboyIntegrationOptions(UserIdMapper userIdMapper) {
    this.userIdMapper = userIdMapper;
  }

  public static class Builder {
    private UserIdMapper userIdMapper;

    public Builder userIdMapper(UserIdMapper userIdMapper) {
      this.userIdMapper = userIdMapper;
      return this;
    }

    public AppboyIntegrationOptions build() {
      return new AppboyIntegrationOptions(userIdMapper);
    }
  }
}
