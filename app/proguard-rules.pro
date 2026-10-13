# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/username/Library/Android/sdk/tools/proguard/proguard-android.txt
-keep class io.github.kg6k.soundamplifier.** { *; }
-keepclassmembers class * extends android.service.quicksettings.TileService { *; }
-keepclassmembers class * extends android.app.Service { *; }
