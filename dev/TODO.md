# TODO

- [x] Add support for Android API level 35
- [x] example icons in setting "Style" for each identicon type on Android 16 are missing
- [x] remove XposedBridge in About
- [x] add a new style "unicornify":

URL format: https://unicornify.pictures/avatar/$HASH?s=$SIZE

$HASH is any hexadecimal number, up to 64 digits.
$SIZE is the requested image size in pixels. It should be a power of 2 between 32 and 128 (inclusive).

Example: https://unicornify.pictures/avatar/7daf6c79d4802916d83f6266e24850af?s=32

If you want bigger and/or higher-quality images, the avatar generation code (including a command-line app) is at https://github.com/balpha/go-unicornify.

Made with <3 by @balpha. Also see @UnicornOfTheDay.