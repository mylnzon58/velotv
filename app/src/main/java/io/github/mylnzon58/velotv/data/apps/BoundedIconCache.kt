package io.github.mylnzon58.velotv.data.apps

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache

/**
 * Bounded LRU bitmap cache sized strictly for 1–2 GB RAM Smart TVs (e.g. Skyworth MT5891).
 * Prevents OOM and GC stalls by capping icon/banner memory to at most 6 MB.
 */
class BoundedIconCache(maxSizeKb: Int = DEFAULT_CACHE_KB) {

    private val cache = object : LruCache<String, Bitmap>(maxSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    fun get(key: String): Bitmap? = cache.get(key)

    fun put(key: String, bitmap: Bitmap) {
        if (bitmap.byteCount / 1024 < cache.maxSize() / 2) {
            cache.put(key, bitmap)
        }
    }

    fun clear() = cache.evictAll()

    fun drawableToBoundedBitmap(
        drawable: Drawable,
        maxWidthPx: Int = 320,
        maxHeightPx: Int = 180
    ): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val src = drawable.bitmap
            if (src.width <= maxWidthPx && src.height <= maxHeightPx) {
                return src
            }
            val scale = minOf(
                maxWidthPx.toFloat() / src.width.coerceAtLeast(1),
                maxHeightPx.toFloat() / src.height.coerceAtLeast(1)
            )
            val w = (src.width * scale).toInt().coerceAtLeast(1)
            val h = (src.height * scale).toInt().coerceAtLeast(1)
            return Bitmap.createScaledBitmap(src, w, h, true)
        }

        val intrinsicW = drawable.intrinsicWidth.takeIf { it > 0 } ?: maxWidthPx
        val intrinsicH = drawable.intrinsicHeight.takeIf { it > 0 } ?: maxHeightPx
        val scale = minOf(
            1f,
            maxWidthPx.toFloat() / intrinsicW,
            maxHeightPx.toFloat() / intrinsicH
        )
        val w = (intrinsicW * scale).toInt().coerceAtLeast(1)
        val h = (intrinsicH * scale).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        private const val DEFAULT_CACHE_KB = 6 * 1024 // 6 MB cap for low-RAM TVs
    }
}

