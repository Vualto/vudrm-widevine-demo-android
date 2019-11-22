## VUDRM Widevine Demo App for Android

- VUDRMWidevine SDK Version: 0.3.5


### Introduction

This demo application demonstrates how to use Vualto's VUDRMWidevine SDK for Android for both online and offline content.

VUDRMWidevine is an Android Archive (AAR) which can be used during the media rendering pipeline to provide a DRM plugin to ExoPlayer 2.9.6 which will work with Vualto DRM workflow. VUDRMWidevine has been developed to specifically manage the session DRM, allowing complete asset and player management.

### Requirements

- A Widevine encrypted video stream.
- The ContentID used when creating the Widevine encrypted video stream.
- Access to our token generation system.
- A valid username and password are required to access the VUDRMWidevine SDK from our Maven repository.
- Minimum Android SDK version is 19 (Android 4.4)
- Maximum Android SDK version is 28 (Android 9)
- Android Studio 3.3.2

You can install new versions of the Android SDK by clicking the SDK manager button ![SDK manager icon](SDKManager.png) which by default located in the top right corner of android studio. Then simply select the version you wish to insall and press "Apply".

### VUDRM
For further information about VUDRM, or for help configuring this demo application, please contact us.

Further information about tokens and the policies used to generate them can be found [here]
(https://docs.vualto.com/projects/vudrm/en/latest/VUDRM-token.html).


Demo Set up
----------

Clone, or download the demo project and unzip.

When you have set up your Widevine encrypted stream, have access to a valid token and the contentID, and have a valid username and password for the SDK repository you should create a file at the root of the demo app project named `gradle.properties`. To enable access to the Maven repository this file should contain the following content:

```
mavenUsername=yourUsername
mavenPassword=yourPassword
```

You may then:

- in the java file named `MainActivity.java`, hard code the URL of the Widevine encrypted stream, the ContentID, and token, then launch the demo application on device and select the *Launch Player* button to play the stream.
- manually enter the URL of the Widevine encrypted stream, the ContentID, and token on device in the running demo application and select the *Launch Player* button to play the stream.

A token generated with a persistent policy is required to use offline playback. This token will allow storage of the license on device and playback of downloaded content.

Build
----------
To build the project start by adding a new configuration. This can be done by pressing the "Add Configuration..." button followed by pressing the "+" icon and selecting "Gradle".
Then set the "Gradle project" to `C:/path/to/repo/app` and `Tasks` to `build`.
Now when you press the run button the project will build.

### Known Issues

- Running this demo applicaton on an Android emulator in Android Studio is not recommended.

- 32-bit devices displayed issues where a license expiry time (secs) is too large to be handled, it therefore returns 0 seconds remaining and considers the license expired. To work around this, always set the license expiry time in your VUDRM token policy.

If you believe you have found any other issue, please contact us at <support@vualto.com>

### Custom Integration

To integrate the VUDRMWidevine SDK into your own Android project please [click here]
(https://docs.vualto.com/projects/vudrm/en/latest/integrations/mobile/android-widevine-sdk.html).


### Looking for iOS?

[Click here] (https://docs.vualto.com/projects/vudrm/en/latest/integrations/mobile/ios-fairplay-sdk.html) to learn more about our iOS VUDRMFairPlay SDK.

### SDK Release Notes

v0.3.5 (build 277) on 07/08/2019

- Update license server DNS
- Replaces instances of assetName and assetID with contentID to be consistent across DRM platform

v0.3.4 (build 272) on 22/05/2019

- Widevine Offline Implementation – Download asset and license, and play downloaded asset offline with saved license.

v0.3.3 (build 243) on 15/05/2019

- Build automation updates

v0.3.2 (build 233) on 17/04/2019

- Update dependencies
- Resolve deprecations
- Update to Android Studio 3.3.2 and SDK 28

v0.3.1 (build 216) on 21/02/2019

- Add Widevine provision callback

v0.3.0 (build 204) on 12/01/2018

- Add Widevine provision callback
- Bug fixes and improvements

v0.2.0 on 12/04/2017

- New major exoplanet release
- Bug fixes and improvements

v0.1.0 on 10/03/2017

- Initial release

