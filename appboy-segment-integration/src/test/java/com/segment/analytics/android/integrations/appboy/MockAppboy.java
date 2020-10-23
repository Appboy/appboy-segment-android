package com.segment.analytics.android.integrations.appboy;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appboy.AppboyUser;
import com.appboy.IAppboy;
import com.appboy.IAppboyImageLoader;
import com.appboy.events.BrazeNetworkFailureEvent;
import com.appboy.events.ContentCardsUpdatedEvent;
import com.appboy.events.FeedUpdatedEvent;
import com.appboy.events.IEventSubscriber;
import com.appboy.events.IValueCallback;
import com.appboy.events.InAppMessageEvent;
import com.appboy.events.SessionStateChangedEvent;
import com.appboy.models.IInAppMessage;
import com.appboy.models.cards.Card;
import com.appboy.models.outgoing.AppboyProperties;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.List;

public class MockAppboy implements IAppboy {
  @Override
  public void openSession(Activity activity) {

  }

  @Override
  public void closeSession(Activity activity) {

  }

  @Override
  public void logCustomEvent(String s) {

  }

  @Override
  public void logCustomEvent(String s, AppboyProperties appboyProperties) {

  }

  @Override
  public void logPurchase(String s, String s1, BigDecimal bigDecimal) {

  }

  @Override
  public void logPurchase(String s, String s1, BigDecimal bigDecimal, AppboyProperties appboyProperties) {

  }

  @Override
  public void logPurchase(String s, String s1, BigDecimal bigDecimal, int i) {

  }

  @Override
  public void logPurchase(String s, String s1, BigDecimal bigDecimal, int i, AppboyProperties appboyProperties) {

  }

  @Override
  public void logPushNotificationOpened(String s) {

  }

  @Override
  public void logPushNotificationOpened(Intent intent) {

  }

  @Override
  public void logPushNotificationActionClicked(String s, String s1, String s2) {

  }

  @Override
  public void logPushStoryPageClicked(String s, String s1) {

  }

  @Override
  public void logContentCardsDisplayed() {

  }

  @Override
  public void logFeedDisplayed() {

  }

  @Override
  public void requestContentCardsRefresh(boolean b) {

  }

  @Override
  public void requestFeedRefresh() {

  }

  @Override
  public void requestFeedRefreshFromCache() {

  }

  @Override
  public void requestImmediateDataFlush() {

  }

  @Override
  public void subscribeToContentCardsUpdates(IEventSubscriber<ContentCardsUpdatedEvent> iEventSubscriber) {

  }

  @Override
  public void subscribeToFeedUpdates(IEventSubscriber<FeedUpdatedEvent> iEventSubscriber) {

  }

  @Override
  public void subscribeToNewInAppMessages(IEventSubscriber<InAppMessageEvent> iEventSubscriber) {

  }

  @Override
  public void subscribeToSessionUpdates(IEventSubscriber<SessionStateChangedEvent> iEventSubscriber) {

  }

  @Override
  public void subscribeToNetworkFailures(IEventSubscriber<BrazeNetworkFailureEvent> iEventSubscriber) {

  }

  @Override
  public <T> void removeSingleSubscription(IEventSubscriber<T> iEventSubscriber, Class<T> aClass) {

  }

  @Override
  public void changeUser(String s) {

  }

  @Override
  public AppboyUser getCurrentUser() {
    return null;
  }

  @Override
  public void getCurrentUser(IValueCallback<AppboyUser> iValueCallback) {

  }

  @Override
  public void registerAppboyPushMessages(String s) {

  }

  @Override
  public String getAppboyPushMessageRegistrationId() {
    return null;
  }

  @Override
  public String getInstallTrackingId() {
    return null;
  }

  @Override
  public IAppboyImageLoader getAppboyImageLoader() {
    return null;
  }

  @Override
  public void setAppboyImageLoader(IAppboyImageLoader iAppboyImageLoader) {

  }

  @Override
  public int getContentCardCount() {
    return 0;
  }

  @Override
  public int getContentCardUnviewedCount() {
    return 0;
  }

  @Override
  public long getContentCardsLastUpdatedInSecondsFromEpoch() {
    return 0;
  }

  @Nullable
  @Override
  public List<Card> getCachedContentCards() {
    return null;
  }

  @Override
  public void setGoogleAdvertisingId(@NonNull String s, boolean b) {

  }

  @Override
  public IInAppMessage deserializeInAppMessageString(String s) {
    return null;
  }

  @Override
  public Card deserializeContentCard(@NonNull String s) {
    return null;
  }

  @Override
  public Card deserializeContentCard(@NonNull JSONObject jsonObject) {
    return null;
  }

  @Override
  public void requestGeofences(double v, double v1) {

  }

  @Override
  public void logFeedCardImpression(String s) {

  }

  @Override
  public void logFeedCardClick(String s) {

  }
}
