# Sound Amplifier (`io.github.kg6k.soundamplifier`)

A lightweight Android volume booster and audio loudness amplifier built with Material 3 principles and Quick Settings Tile support.

## Key Features

- **Intuitive Boost Slider**: Amplifies media volume up to +100% past normal speaker limits.
- **Background Foreground Service**: Continues running across music streaming, podcasts, and games via `ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK`.
- **Quick Settings Tile**: Fast one-tap toggle and real-time percentage subtitle in the notification shade via `TileService`.
- **Dynamic Safety Limiter**: Prevents harsh clipping distortion and protects device speakers.
- **Android 10+ Compatible**: Designed for `minSdk = 29` (Android 10) through `targetSdk = 35` (Android 15).

## Standard Project Structure

```
.
├── .github/                 # GitHub workflows & CI configuration
├── app/                     # Main Android application module
│   ├── build.gradle.kts     # App build configuration (package io.github.kg6k.soundamplifier)
│   ├── proguard-rules.pro   # Proguard / R8 optimization rules
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/io/github/kg6k/soundamplifier/
│       │   ├── MainActivity.kt
│       │   ├── VolumeBoosterService.kt
│       │   └── VolumeBoosterTileService.kt
│       └── res/
│           ├── drawable/
│           └── values/
├── gradle/                  # Gradle version catalog & wrapper
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts         # Root build configuration
├── settings.gradle.kts      # Project settings & module inclusion
├── .editorconfig            # Code style formatting
├── .gitattributes           # Git line-ending configuration
├── CONTRIBUTING.md          # Contribution guidelines
└── AGENTS.md                # AI agent instructions
```
