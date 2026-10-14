# Keep class metadata and attributes for Compose & reflection
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep all project application, activities, services, receivers, and UI composables
-keep class io.github.kg6k.soundamplifier.** { *; }
-keepclassmembers class * extends android.service.quicksettings.TileService { *; }
-keepclassmembers class * extends android.app.Service { *; }
-keepclassmembers class * extends android.content.BroadcastReceiver { *; }

# Keep Audio Effects and Audio Policy interfaces intact
-keep class android.media.audiofx.** { *; }
-keepclassmembers class android.media.audiofx.** { *; }
-dontwarn android.media.audiofx.**
