# VeloTV R8 / ProGuard rules (KISS & safe)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-dontwarn com.google.zxing.**
-keep class com.google.zxing.qrcode.** { *; }
-keep class com.google.zxing.common.BitMatrix { *; }
-keep class com.google.zxing.BarcodeFormat { *; }
-keep class com.google.zxing.EncodeHintType { *; }

