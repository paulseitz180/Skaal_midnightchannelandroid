# Midnight Channel — R8 / ProGuard rules (release)
#
# Keep rules are minimal: Compose + Kotlin + WebView reflection surfaces only.
# Do not weaken optimization for convenience.

# —— Kotlin / coroutines ——
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**

# —— AndroidX / Compose (AGP consumer rules cover most; keep ViewModel/Fragment-free) ——
-keep class androidx.compose.runtime.** { *; }

# —— WebView / Chromium bridge ——
# Preserve client/chrome overrides and any JS interfaces if added later.
-keepclassmembers class * extends android.webkit.WebViewClient {
    public *;
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public *;
}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# —— SplashScreen / AppCompat bridge used by androidx.core:core-splashscreen ——
-keep class androidx.core.splashscreen.** { *; }

# —— Enums used in sealed-state style serialization paths (safety) ——
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# —— Strip verbose logs in release (Log.d / Log.v) ——
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
