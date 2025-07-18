LICENSE FOR THE MAIN PRODUCT
============================

Original work Copyright (C) 2013 The ChameleonOS Open Source Project

Modified work Copyright (C) 2013-2014 GermainZ@xda-developers.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


INCLUDED LIBRARIES
==================

Android Support v4 Libraries:
-----------------------------
     Copyright (c) 2005-2008, The Android Open Source Project
     Licensed under the Apache License, Version 2.0

Holo Color Picker library
-------------------------
<https://github.com/LarsWerkman/HoloColorPicker>

     Copyright 2012 Lars Werkman
     Licensed under the Apache License, Version 2.0


IDENTICON STYLES
================

Retro Style
-----------
     Original implementation in ChameleonOS
     Inspired by 8-bit retro game sprites
     Generated locally on device from a hash of contact details

Contemporary Style
-----------------
     Original implementation in ChameleonOS
     Modern, abstract geometric identicon style
     Generated locally on device from a hash of contact details

Gmail Style
-----------
     Inspired by Google Mail/Gmail's contact icons
     Simple colored letter icon style
     Generated locally on device using the contact's initials

Dot Matrix Style
--------------
     Original implementation in ChameleonOS
     Pixel-based grid pattern similar to GitHub's original identicons
     Generated locally on device from a hash of contact details

Spirograph Style
--------------
     Original implementation in ChameleonOS
     Creates geometric circular patterns similar to Spirograph toys
     Generated locally on device from a hash of contact details

Unicornify Style
--------------
     Based on unicornify.pictures by @balpha
     <https://github.com/balpha/go-unicornify>
     
     This style downloads unicorn avatar images from an online service.
     Images are dynamically fetched from https://unicornify.pictures/avatar/ when needed,
     using the contact's hash to generate a unique unicorn avatar. Each request includes
     the hash and desired image size. The images are not stored permanently on the server
     but are generated algorithmically for each request.
     
     NOTE: Unicorn icons are cached locally for offline use after they are first downloaded.
     An internet connection is only required the first time an avatar is generated.
     When offline, the app will display a warning message and use previously cached avatars.
     If no cached avatar is available for a contact, the app will skip that contact and
     preserve any existing photo