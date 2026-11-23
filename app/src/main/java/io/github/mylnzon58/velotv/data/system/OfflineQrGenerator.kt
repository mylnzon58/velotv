package io.github.mylnzon58.velotv.data.system

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Generates high-contrast QR codes 100% offline in memory.
 * Zero network calls, crisp scanning from 3 meters away on a TV screen.
 */
object OfflineQrGenerator {

    fun generate(content: String, sizePx: Int = 360): Bitmap? = runCatching {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 2,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val pixels = IntArray(sizePx * sizePx)
        for (y in 0 until sizePx) {
            val rowOffset = y * sizePx
            for (x in 0 until sizePx) {
                pixels[rowOffset + x] = if (matrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565).apply {
            setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
        }
    }.getOrNull()
}

