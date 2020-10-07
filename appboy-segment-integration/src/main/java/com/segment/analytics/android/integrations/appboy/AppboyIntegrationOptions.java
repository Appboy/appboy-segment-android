package com.segment.analytics.android.integrations.appboy;

public class AppboyIntegrationOptions {

  private UserIdMapper userIdMapper;
  private boolean enableTraitDiffing;

  public static Builder builder() {
    return new Builder();
  }

  UserIdMapper getUserIdMapper() {
    return userIdMapper;
  }

  public boolean isTraitDiffingEnabled() {
    return enableTraitDiffing;
  }

  private AppboyIntegrationOptions(UserIdMapper userIdMapper, boolean enableTraitDiffing) {
    this.userIdMapper = userIdMapper;

    this.enableTraitDiffing = enableTraitDiffing;
  }

  public static class Builder {
    private UserIdMapper userIdMapper;
    private boolean traitDiffingEnabled;

    public Builder userIdMapper(UserIdMapper userIdMapper) {
      this.userIdMapper = userIdMapper;
      return this;
    }

    public Builder enableTraitDiffing(boolean enable) {
      this.traitDiffingEnabled = enable;
      return this;
    }

    public AppboyIntegrationOptions build() {
      return new AppboyIntegrationOptions(userIdMapper, traitDiffingEnabled);
    }
  }
}
