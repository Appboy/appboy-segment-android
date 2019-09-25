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
