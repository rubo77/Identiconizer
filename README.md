Identiconizer!
==============
This is a port of ChameleonOS' contact identicons feature (available in the
JellyBean versions) with some additional features and fixes.
When enabled, new contacts will be assigned a unique identicon instead of the
default picture.

XDA Thread
==========
http://forum.xda-developers.com/showthread.php?t=2718943

Development
===========
If you want to help developing the app, this will work as an example (tested on Ubuntu 19.10):

- Install and open Android Studio
- [Created a virtual AVD Device](https://developer.android.com/studio/run/managing-avds#createavd)
- use File->New->Project from version control->Git
- Wait for "Refreshing Identiconizer Gradle project" and "Updating Indices"
- Run the App
- Inside the simulator window: allow to access Contacts
- use the Call Button to call any number and cancel
- add the last Number as new contact and store it locally
- Now the contact App works without google account and you can add more dummy contacts
