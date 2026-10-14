# Sound Amplifier Android Release Signing Key

This directory stores the official, permanent release keystore used to sign the **Sound Amplifier** Android application (`io.github.kg6k.soundamplifier`).

## Why a Fixed Signing Key is Essential

In Android's security model, every APK package is cryptographically bound to its signing certificate:
1. **Seamless App Updates**: Android allows updates to an existing installed app *only* if the incoming update is signed with the exact same certificate as the currently installed version.
2. **Signature Mismatches**: If an app is built on an ephemeral runner (such as GitHub Actions) using temporary debug keys, each runner generates a different key. When users try to install the updated APK, Android rejects the install with `Package signatures do not match previously installed version`.
3. **Identity & Integrity**: A permanent release key guarantees that the APK was produced by the official repository and has not been tampered with.

## Keystore Specification

| Property | Value |
|---|---|
| **Keystore File** | `keystore/release.jks` |
| **Keystore Format** | PKCS12 |
| **Key Alias** | `soundamplifier` |
| **Key Algorithm** | RSA 2048-bit |
| **Signature Algorithm** | SHA256withRSA |
| **Validity** | 10,000 days (Valid until March 2054) |
| **Subject (DN)** | `CN=Sound Amplifier, OU=Mobile, O=Sound Amplifier, L=Global, ST=Global, C=US` |
| **SHA-256 Fingerprint** | `09:0B:D9:45:02:8E:CF:0F:FA:23:A6:D8:CE:73:68:3D:B3:FD:0D:01:DF:14:2D:84:18:47:3E:D3:AF:50:AC:A1` |
| **SHA-1 Fingerprint** | `AE:E4:EC:29:40:0C:6F:56:9C:3D:83:3A:45:E4:28:34:88:1C:1E:F0` |

## Gradle Configuration

Configured in `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = rootProject.file("keystore/release.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "soundamplifier"
        keyAlias = System.getenv("KEY_ALIAS") ?: "soundamplifier"
        keyPassword = System.getenv("KEY_PASSWORD") ?: "soundamplifier"
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        // ...
    }
}
```
Environment variables `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` can optionally be passed to override passwords if desired.
