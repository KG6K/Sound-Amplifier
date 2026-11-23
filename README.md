# Sound Amplifier

An open-source, copylefted Sound Amplifier for Android.

## Features

- **Boost Amplification Slider**: Adjusts target gain from `0%` to `100%` (up to `+60 dB`) using Android's built-in `LoudnessEnhancer` audio effect API, paired with a separate System Media Volume slider.
- **Quick Turn On / Turn Off**: Toggle amplification from the app header while remembering the last used boost percentage.
- **Foreground Service**: Runs as an Android `mediaPlayback` foreground service applying `LoudnessEnhancer` to global audio session `0` and media player audio sessions. Actual loudness gain and app compatibility depend on the device's audio hardware and Android implementation.
- **Quick Settings Tile** (Android 7.0+): Toggles amplification directly from Quick Settings on Android 12+ (falling back to opening the app if restricted by the OS), opens the app on Android 7.0–11, and shows the active boost level in the tile subtitle on Android 10+.
- **Visual High-Boost Warning**: Highlights the boost indicator in red above `60%` alongside an on-screen warning. High amplification can cause distortion and may damage speakers or hearing—use at your own risk.
- **Android Compatibility**: Supports Android 6.0 Marshmallow (`minSdk = 23`) through Android 15 (`targetSdk = 35`).

## Built With

This app was built using Google AI Studio.

## License

Sound Amplifier is licensed under the [GNU General Public License v3.0](LICENSE) (GPL-3.0).
