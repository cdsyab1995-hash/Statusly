# Keep Room's generated database implementation and schema metadata.
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Database class * { *; }

# Hilt and generated dependency graph classes.
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class *_HiltModules_* { *; }
-keep class *Hilt* { *; }

# Kotlin metadata used by reflection-aware AndroidX libraries.
-keep class kotlin.Metadata { *; }
