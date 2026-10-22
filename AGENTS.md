# Agent Instructions for Sound Amplifier

- **Package**: `io.github.kg6k.soundamplifier`
- **Minimum SDK**: 23 (Android 6.0)
- **Target SDK**: 35 (Android 15)
- **Architecture**:
  - `VolumeBoosterService`: Android Foreground Service (`foregroundServiceType="mediaPlayback"`)
  - `VolumeBoosterTileService`: Android Quick Settings `TileService` (`android.service.quicksettings.action.QS_TILE`)
  - `MainActivity`: Jetpack Compose UI with dual volume and boost amplification sliders
