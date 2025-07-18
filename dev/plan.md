# Identiconizer Project Plan

## Notes
- Android 12+ requires specifying PendingIntent mutability (FLAG_IMMUTABLE or FLAG_MUTABLE).
- Errors are present in IdenticonCreationService.java and IdenticonRemovalService.java at lines where PendingIntent.getActivity is used without a flag.
- FLAG_IMMUTABLE is usually preferred unless the PendingIntent needs to be mutable.
- Patch version increased: versionName is now 1.4.1
- Crash when adding contact pic due to missing FOREGROUND_SERVICE permission
- Android 12+ (targetSdk 31+) requires specifying foregroundServiceType when starting a foreground service
- foregroundServiceType added to IdenticonCreationService and IdenticonRemovalService in AndroidManifest.xml
- Android 13+ with dataSync foreground service type requires FOREGROUND_SERVICE_DATA_SYNC permission
- TODO.md contains additional roadmap items (API 35, icons in Style, remove XposedBridge, add unicornify style)
- Build fixed: UnicornifyIdenticon now implements all required Identicon methods
- Crash when using unicornify style due to missing INTERNET permission
- INTERNET permission added to AndroidManifest.xml to support unicornify style
- Unicorn avatars (unicornify style) are NOT cached for offline use; they are downloaded on demand each time needed.
- Documentation and README updated: identicon style info, unicornify explanation, F-Droid/feature details added.
- Unicorn avatars (unicornify style) should be cached locally for offline use. When offline, the app should show a warning and not replace existing identicons with blank images.

## Task List
- [x] Review all usages of PendingIntent.getActivity in IdenticonCreationService.java and IdenticonRemovalService.java
- [x] Update each usage to specify FLAG_IMMUTABLE (or FLAG_MUTABLE if required)
- [x] Increase patch version in build.gradle (versionName)
- [x] Add FOREGROUND_SERVICE permission to AndroidManifest.xml
- [x] Add foregroundServiceType to IdenticonCreationService and IdenticonRemovalService in AndroidManifest.xml
- [x] Update IdenticonCreationService implementation to use startForeground with foregroundServiceType if required
- [x] Update IdenticonRemovalService implementation to use startForeground with foregroundServiceType if required and import ServiceInfo if needed
- [x] Add FOREGROUND_SERVICE_DATA_SYNC permission to AndroidManifest.xml
- [x] Rebuild the APK and verify lint passes
- [x] Add support for Android API level 35 (commit separately)
- [x] Add example icons in Style setting for each identicon type on Android 16 (commit separately)
- [x] Remove XposedBridge in About (commit separately)
- [x] Add new style "unicornify" using unicornify.pictures avatar service (commit separately)
- [x] Integrate UnicornifyIdenticon into IdenticonFactory
- [x] Add unicornify style to style selection UI and resources
- [x] Implement generateIdenticonByteArray(String) in UnicornifyIdenticon to fix build
- [x] Add INTERNET permission to AndroidManifest.xml for unicornify style
- [ ] Cache unicornify avatars locally for offline use
- [ ] Show warning when offline and unicornify cannot download, do not replace existing identicons with blank images

## Current Goal
All tasks complete except for unicornify offline caching and warning behavior.