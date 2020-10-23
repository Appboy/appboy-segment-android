## 7.0.0

##### Breaking
- Updated to [Braze Android SDK 10.0.0](https://github.com/Appboy/appboy-android-sdk/blob/master/CHANGELOG.md#1000).

## 6.0.0

##### Breaking
- Updated to [Braze Android SDK 9.0.0](https://github.com/Appboy/appboy-android-sdk/blob/master/CHANGELOG.md#900).

## 5.0.0

##### Fixed
- Fixes an issue where purchases would be logged for any `track` payload with `products` included. After this fix, a `track` payload must either contain `revenue` or have the event name `Order Completed` to result in a logged purchase to Braze.
- Fixes an issue where `anonymousId` and `userId` could be set as custom attributes when calling `identify()`.

## 4.0.0

##### Breaking
- Log separate purchases for each `product` in the `products` array in a `track` call.
  - In the past we always used the event name as the Braze product ID.
  - Now if a track call has the event name `Order Completed` or key `revenue` included in `properties` and has a `products` array, we will log each object in the array as a separate purchase using `productId` as the Braze product ID. `price` and `quantity` will be read from the individual array if available and all non-Braze recognized fields from the high level `properties` and each individual array will be combined and sent as event properties.
  - If there is no `products` array we will continue using the event name as the Braze product ID if the key `revenue` is included in `properties`.

## 3.0.0

##### Breaking
- Updated to [Braze Android SDK 6.0.0](https://github.com/Appboy/appboy-android-sdk/blob/master/CHANGELOG.md#600).

##### Changed
- The `campaign` property value is now casted to `ValueMap` instead of its `Properties` subclass.
  - See https://github.com/Appboy/appboy-segment-android/pull/19. Thanks @ciaranmul!
- The `appboy-sample` sample app is now located within this repo instead of a separate standalone repo.
- The SDK code has been moved into a directory called `appboy-segment-integration`.

## 2.5.2

##### Fixed
- Fixed potential for null pointer exception in the `identify()` method resulting from a null value for `Appboy.getCurrentUser()`.

## 2.5.1

##### Added
- Added support for `double` typed `Traits` values.

## 2.5.0

##### Breaking
- Updated to [Braze Android SDK 3.7.1](https://github.com/Appboy/appboy-android-sdk/blob/master/CHANGELOG.md#371).
- Removed the Feedback feature.

## 2.4.1

##### Fixed
- Fixed an issue where the following default attributes would also be logged as custom attributes when set through a call to `identify()`: "birthday", "email", "firstName", "lastName", "gender", "phone", "address".

## 2.4.0

##### Breaking
- Updated to [Braze Android SDK 3.3.0](https://github.com/Appboy/appboy-android-sdk/blob/master/CHANGELOG.md#330).

## 2.3.0

##### Breaking
- Updated to [Braze Android SDK 3.2.0](https://github.com/Appboy/appboy-android-sdk/blob/master/CHANGELOG.md#321).
  - Added `AppboyFirebaseMessagingService` to directly use the Firebase messaging event `com.google.firebase.MESSAGING_EVENT`. This is now the recommended way to integrate Firebase push with Braze. The `AppboyFcmReceiver` should be removed from your `AndroidManifest` and replaced with the following:
    ```
    <service android:name="com.appboy.AppboyFirebaseMessagingService">
      <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
      </intent-filter>
    </service>
    ```
    - Also note that any `c2dm` related permissions should be removed from your manifest as Braze does not require any extra permissions for `AppboyFirebaseMessagingService` to work correctly.

## 2.2.0
* Uses analytics-android 4.3.1 and Braze Android SDK 3.0.1

## 2.1.2
* Uses analytics-android 4.3.1 and Braze Android SDK 2.2.5

## 2.1.1
* Uses analytics-android 4.3.1 and Braze Android SDK 2.2.1
* Fixes an issue where an Install Attributed event in the wrong format could cause a crash.

## 2.1.0
* Uses analytics-android 4.2.6 and Braze Android SDK 2.1.2
  * Braze Android SDK 2.1.1 supports Android O. Please see our [Braze SDK Changelog](https://github.com/Appboy/appboy-android-sdk/blob/master/CHANGELOG.md#added-1) for more information on this.
* Adds support for custom endpoints which can be set on the Segment dashboard.
* Fixes a bug where birthdays weren't being sent up to our servers properly.

## 2.0.2
* Fixes a bug where an "Install Attributed" event without any data was causing a crash.

## 2.0.1
* Uses analytics-android 4.2.6 and Braze Android SDK 2.0.4
* Fixes an issue where install attribution data was being sent up as an event.

## 2.0.0
* Uses analytics-android 4.2.6 and Braze Android SDK 2.0.0
* Adds support for custom attribute values with date types.

## 1.1.0
* Uses analytics-android 4.2.0 and Braze Android SDK 1.15.1

## 1.0.1
* Uses Braze Android SDK 1.13.2

## 1.0.0
*  (Supports analytics-android 4.0.+ and Braze Android SDK 1.11.+)*
*  Initial Release
