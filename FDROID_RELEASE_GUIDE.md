# F-Droid Release Guide for Sound Amplifier

This document outlines the step-by-step process for submitting **Sound Amplifier** to the official **F-Droid Main Repository** (`fdroiddata`).

---

## Prerequisites (Already Prepared in Repository)

The repository has been structured to satisfy all F-Droid inclusion requirements:
- [x] **Open Source Copyleft License**: Full text of GNU General Public License v3.0 in `LICENSE`.
- [x] **Zero Proprietary Blobs**: 100% pure Kotlin, AndroidX, Jetpack Compose, and native Android `LoudnessEnhancer`.
- [x] **Fastlane Metadata**: Store listing, descriptions, and changelog in `fastlane/metadata/android/en-US/`.
- [x] **F-Droid Build Recipe**: Ready-to-submit YAML file in `fdroid/io.github.kg6k.soundamplifier.yml`.
- [x] **Android 6.0 to 15 Support**: `minSdk = 23`, `targetSdk = 35`.

---

## Step 1: Create a Git Release Tag on GitHub

F-Droid builds releases from signed/annotated Git tags.

1. Ensure your latest changes are pushed to `main`.
2. Create and push a Git tag matching the version (`v1.0.0-alpha2`):
   ```bash
   git tag -a v1.0.0-alpha2 -m "Release v1.0.0-alpha2"
   git push origin v1.0.0-alpha2
   ```

---

## Step 2: Fork `fdroiddata` on GitLab

F-Droid maintains its catalog of apps on GitLab: [https://gitlab.com/fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata).

1. Go to [https://gitlab.com/fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata).
2. Click **Fork** (top-right) into your personal GitLab account (free to create if you don't already have one).

---

## Step 3: Add the Recipe File to Your Fork

1. In your forked repository (`gitlab.com/<your-username>/fdroiddata`):
2. Navigate to the `metadata/` folder.
3. Click **+** $\rightarrow$ **New file**.
4. Set the file path to:
   ```
   metadata/io.github.kg6k.soundamplifier.yml
   ```
5. Copy and paste the contents from [`fdroid/io.github.kg6k.soundamplifier.yml`](./fdroid/io.github.kg6k.soundamplifier.yml).
6. Set the commit message:
   ```
   Add io.github.kg6k.soundamplifier
   ```
7. Click **Commit changes**.

---

## Step 4: Open a Merge Request (MR)

1. On GitLab, go to **Merge requests** $\rightarrow$ **New merge request**.
2. Select:
   - Source branch: `<your-username>/fdroiddata:master`
   - Target branch: `fdroid/fdroiddata:master`
3. Click **Compare branches and continue**.
4. Set the MR title:
   ```
   Add io.github.kg6k.soundamplifier
   ```
5. In the description, F-Droid provides a standard checklist:
   - Mark the checkboxes confirming the app is open source and contains no proprietary binaries.
   - Mention that you are the author/maintainer (`KG6K/Sound-Amplifier`).
6. Click **Create merge request**.

---

## Step 5: F-Droid Automated Review & Publishing

1. **Automated CI**: F-Droid's build bot (`@fdroid-bot`) will run automated linting (`fdroid checkupdates`, `fdroid lint`) on your MR.
2. **Reviewer Feedback**: An F-Droid team reviewer will verify the license and build recipe.
3. **Merge & Build**: Once merged, F-Droid's automated build server will:
   - Clone your tag from GitHub.
   - Compile the APK using their own isolated build environment.
   - Sign the APK with F-Droid's official release keys.
   - Publish the app in the official F-Droid catalog within 24–48 hours!

---

## Optional: Faster Alternative via IzzyOnDroid

If you want immediate availability while waiting for the official F-Droid review:
1. Submit an issue to [IzzyOnDroid](https://gitlab.com/IzzyOnDroid/repo/-/issues).
2. IzzyOnDroid indexes APKs directly from your GitHub Releases without rebuilding from source.
