# Application specific proguard rules

# Open source app - no need to obfuscate
-dontobfuscate

# Specific optimizations - same as stock but added !code/allocation/variable
# to get around post Proguard Dex failure
#
# Refer - http://proguard.sourceforge.net/manual/troubleshooting.html#simexception
# and http://stackoverflow.com/questions/5701126/compile-with-proguard-gives-exception-local-variable-type-mismatch
#
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*,!code/allocation/variable

# Adding this in to preserve line numbers so that the stack traces can be remapped
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Remove low level logging
-assumenosideeffects class android.util.Log {
    public static *** i(...);
    public static *** d(...);
    public static *** v(...);
}

