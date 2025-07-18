Identiconizer!
==============
This is a port of ChameleonOS' contact identicons feature (available in the
JellyBean versions) with some additional features and fixes.
When enabled, new contacts will be assigned a unique identicon instead of the
default picture.

Features
========
* Use identicons for newly created contacts. A service is normally used to detect new contacts. If you use the Xposed Framework, you can enable Identiconizer! as a module instead to integrate the application into the system.
* Choose from six different identicon styles: Retro, Contemporary, Spirograph, Dot Matrix, Gmail, and Unicornify.
* Specify the identicon sizes, from 96x96 up to 720x720 (256x256 max on ICS.)
* Choose a custom background color for the created identicons.
* Option to use serif fonts in Gmail style identicons.
* Option for more than one letter in Gmail Style identicons.
* Create identicons for all contacts without a photo in one go.
* Remove identicons from all contacts that have one set.
* Contacts list to add/remove Identicon to/from wanted contacts only.
* Short delay before creating identicons for new contacts, to avoid overwriting DAVdroid photos.

What's New in Version 1.4.1
========================
* Support for Android API level 35
* Fixed missing example icons for identicon styles on Android 16
* Removed XposedBridge reference from About section
* Added new "unicornify" style using unicornify.pictures avatar service (requires internet connection)
* Added proper permissions for foreground services on Android 12+

What's New in Version 1.4
========================
* The app now targets / works on the latest Android version
* The app's design is now a bit more modern
* Add colors to match Google Messenger theme (contributed by @TiiXel)
* Add serif font support in options (contributed by @TiiXel)
* Add option for more than one letter in Gmail Style (contributed by @TiiXel)
* Add short delay before creating identicons for new contacts, to avoid overwriting DAVdroid photos
* App version is now shown in the About screen

Links
=====
* [XDA Thread](http://forum.xda-developers.com/showthread.php?t=2718943)
* [F-Droid Page](https://f-droid.org/packages/com.germainz.identiconizer/)

Development
===========
If you want to help developing the app, this will work as an example (tested on Ubuntu 24.04):

- Install and open Android Studio
- [Created a virtual AVD Device](https://developer.android.com/studio/run/managing-avds#createavd)
- use File->New->Project from version control->Git
- Wait for "Refreshing Identiconizer Gradle project" and "Updating Indices"
- Run the App
- Inside the simulator window: allow to access Contacts
- use the Call Button to call any number and cancel
- add the last Number as new contact and store it locally
- Now the contact App works without google account and you can add more dummy contacts

Licensing and Attributions
=========================
For detailed information about licensing, included libraries, and identicon styles, please see the [NOTICE.md](NOTICE.md) file.
