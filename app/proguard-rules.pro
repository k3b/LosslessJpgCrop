-dontnote MobileAds

###############
# I use proguard only to remove unused stuff and to keep the app small.
# I donot want to obfuscate (rename packages, classes, methods, ...) since this is open source
-dontobfuscate
-dontoptimize
-keepnames class ** { *; }
-keepnames interface ** { *; }
-keepnames enum ** { *; }
