# Nour (NUR-031) — minimal keep rules for the minified release build.
# Do NOT add blanket -keep ** rules; each line below protects a specific
# entry point that R8 cannot infer.

# Compose / Material 3 (entry points are referenced by name at runtime).
-keep class androidx.compose.** { *; }
-keep class com.google.android.material.** { *; }

# Room: keep the generated database implementation and DAO interfaces.
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class androidx.room.** { *; }

# Adhan scheduling: receivers / services are manifest entry points.
-keep class com.example.features.adhan.** { *; }
-keep class com.example.NourApp { *; }

# Widgets: app-widget provider (manifest entry point).
-keep class com.example.features.widgets.** { *; }

# Quran corpus: parseIntable regexes / reflection-free but keep the data classes.
-keep class com.example.features.quran.** { *; }

# Keep line numbers for crash reports (CrashReporter writes stack traces).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile