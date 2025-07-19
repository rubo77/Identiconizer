Identiconizer!
==============
This is a port of ChameleonOS' contact identicons feature (available in the
JellyBean versions) with some additional features and fixes.
When enabled, new contacts will be assigned a unique identicon instead of the
default picture.

Features
========
* Use identicons for newly created contacts. A background service is used to detect new contacts and automatically assign them an identicon.
* Choose from seven different identicon styles: Retro, Contemporary, Spirograph, Dot Matrix, Gmail, Unicornify, and Visiglyphs.
* Specify the identicon sizes, from 96x96 up to 720x720 (256x256 max on ICS.)
* Choose a custom background color for the created identicons.
* Option to use serif fonts in Gmail style identicons.
* Option for more than one letter in Gmail Style identicons.
* Create identicons for all contacts without a photo in one go.
* Remove identicons from all contacts that have one set.
* Contacts list to add/remove Identicon to/from wanted contacts only.
* Short delay before creating identicons for new contacts, to avoid overwriting DAVdroid photos.

How Identicons Work
==================

## Storage and Detection

**Where are identicons stored?**
Identicons are stored directly in Android's ContactsContract database as contact photos, just like regular profile pictures. They are saved in the `ContactsContract.Data` table with the MIME type `vnd.android.cursor.item/photo` in the `DATA15` field as binary data (byte arrays).

**How does the app distinguish identicons from user photos?**
The app uses a clever tagging system to identify its own generated identicons:
- Each generated identicon contains a special marker string `"identicon_marker"` embedded in the image metadata
- For PNG images: The marker is embedded as a custom chunk at the end of the file
- For JPEG images: The marker is embedded in the EXIF data
- When processing contacts, the app checks for this marker using `IdenticonUtils.isIdenticon()` to determine if an existing photo is an identicon or a user-uploaded image

**Which contacts get identicons?**
The app only replaces photos for contacts that:
- Have no existing photo, OR
- Have an existing identicon (when updating/changing styles)
- Are in visible contact groups (unless "ignore visibility" is enabled)
- Have a non-empty display name

User-uploaded photos are never replaced - the app respects custom profile pictures.

## Contact Sharing and Export

**Are identicons exported when sharing contacts?**
Yes! Since identicons are stored as standard contact photos in Android's database, they are included when:
- Sharing contacts via Android's built-in sharing mechanisms
- Exporting contacts to VCF (vCard) files
- Syncing contacts with cloud services (Google Contacts, Exchange, etc.)
- Backing up contacts

The identicons will appear as regular profile pictures to other devices and applications, since they are stored using Android's standard photo storage format.

**What happens on the receiving device?**
- If the receiving device has Identiconizer installed: The app will recognize the identicon marker and can manage/replace these images
- If the receiving device doesn't have Identiconizer: The identicons will appear as normal profile pictures and remain unchanged

## Technical Implementation

**Contact Detection Process:**
1. The app monitors the ContactsContract database for new contacts
2. When a new contact is detected, it checks if the contact has an existing photo
3. If no photo exists, or if the existing photo is an identicon, a new identicon is generated
4. The identicon is created based on the contact's display name using MD5 hashing
5. The generated image is tagged with the identicon marker and stored in the database

**Offline Behavior:**
- Most identicon styles work offline (Retro, Contemporary, Spirograph, Dot Matrix, Gmail, Visiglyphs)
- The Unicornify style requires internet connection but caches downloaded avatars for offline use
- When offline, Unicornify will use cached versions or skip contacts if no cache exists

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
