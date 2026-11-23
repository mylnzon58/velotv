package io.github.mylnzon58.velotv.ui.tools

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import io.github.mylnzon58.velotv.R

/**
 * Full-screen 4K / HDR display calibration test suite rendered natively in Canvas (0 KB assets).
 * Controls: Left/Right switch pattern, OK toggles HUD overlay, Back exits.
 */
class CalibrationActivity : AppCompatActivity() {

    private lateinit var patternView: CalibrationPatternView
    private var currentPattern = 0
    private var showHud = true

    private val patternNames = listOf(
        "01 / Escala de grises 11 pasos + PLUGE",
        "02 / Barras de color SMPTE",
        "03 / Geometría 16:9 y Overscan (5% · 10%)",
        "04 / Uniformidad — Negro puro",
        "05 / Uniformidad — Gris 50%",
        "06 / Uniformidad — Blanco 100%",
        "07 / Píxeles — Rojo",
        "08 / Píxeles — Verde",
        "09 / Píxeles — Azul",
        "10 / Nitidez y lectura a 3 metros"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        patternView = CalibrationPatternView(this)
        setContentView(patternView)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                currentPattern = (currentPattern + 1) % patternNames.size
                patternView.invalidate()
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                currentPattern = (currentPattern - 1 + patternNames.size) % patternNames.size
                patternView.invalidate()
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                showHud = !showHud
                patternView.invalidate()
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                finish()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    inner class CalibrationPatternView(context: Context) : View(context) {
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = ResourcesCompat.getFont(context, R.font.barlow_medium)
            color = 0xFFEDE8E1.toInt()
            textSize = 26f
        }
        private val displayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = ResourcesCompat.getFont(context, R.font.barlow_condensed_semibold)
            color = 0xFFEDE8E1.toInt()
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            when (currentPattern) {
                0 -> drawGrayscalePluge(canvas, w, h)
                1 -> drawSmpteBars(canvas, w, h)
                2 -> drawOverscanGrid(canvas, w, h)
                3 -> canvas.drawColor(Color.BLACK)
                4 -> canvas.drawColor(Color.rgb(128, 128, 128))
                5 -> canvas.drawColor(Color.WHITE)
                6 -> canvas.drawColor(Color.RED)
                7 -> canvas.drawColor(Color.GREEN)
                8 -> canvas.drawColor(Color.BLUE)
                9 -> drawSharpnessChart(canvas, w, h)
            }
            if (showHud) drawHud(canvas, w, h)
        }

        private fun drawGrayscalePluge(c: Canvas, w: Float, h: Float) {
            val steps = 11
            val bw = w / steps
            val topH = h * 0.72f
            for (i in 0 until steps) {
                val v = ((i * 255f) / (steps - 1)).toInt()
                fillPaint.color = Color.rgb(v, v, v)
                c.drawRect(i * bw, 0f, (i + 1) * bw + 1f, topH, fillPaint)
            }
            // Bottom PLUGE near-black reference bars (0%, 2%, 4%, 6%, 8%)
            fillPaint.color = Color.BLACK
            c.drawRect(0f, topH, w, h, fillPaint)
            val nearBlacks = intArrayOf(0, 5, 10, 16, 22, 32)
            val pw = w / nearBlacks.size
            nearBlacks.forEachIndexed { idx, v ->
                fillPaint.color = Color.rgb(v, v, v)
                c.drawRect(idx * pw, topH + 24f, (idx + 1) * pw, h - 24f, fillPaint)
            }
        }

        private fun drawSmpteBars(c: Canvas, w: Float, h: Float) {
            val topColors = intArrayOf(
                Color.rgb(191, 191, 191),
                Color.rgb(191, 191, 0),
                Color.rgb(0, 191, 191),
                Color.rgb(0, 191, 0),
                Color.rgb(191, 0, 191),
                Color.rgb(191, 0, 0),
                Color.rgb(0, 0, 191)
            )
            val bw = w / topColors.size
            val topH = h * 0.72f
            topColors.forEachIndexed { i, col ->
                fillPaint.color = col
                c.drawRect(i * bw, 0f, (i + 1) * bw + 1f, topH, fillPaint)
            }
            val midColors = intArrayOf(
                Color.rgb(0, 0, 191), Color.rgb(19, 19, 19), Color.rgb(191, 0, 191),
                Color.rgb(19, 19, 19), Color.rgb(0, 191, 191), Color.rgb(19, 19, 19),
                Color.rgb(191, 191, 191)
            )
            midColors.forEachIndexed { i, col ->
                fillPaint.color = col
                c.drawRect(i * bw, topH, (i + 1) * bw + 1f, h * 0.82f, fillPaint)
            }
            fillPaint.color = Color.rgb(16, 16, 16)
            c.drawRect(0f, h * 0.82f, w, h, fillPaint)
        }

        private fun drawOverscanGrid(c: Canvas, w: Float, h: Float) {
            c.drawColor(0xFF12100E.toInt())
            strokePaint.color = 0xFF332E29.toInt()
            strokePaint.strokeWidth = 1.5f
            val step = 60f
            var x = 0f
            while (x <= w) {
                c.drawLine(x, 0f, x, h, strokePaint)
                x += step
            }
            var y = 0f
            while (y <= h) {
                c.drawLine(0f, y, w, y, strokePaint)
                y += step
            }
            // Outer 0% edge (Amber), 5% safe zone (Ivory), 10% title safe (Muted)
            strokePaint.strokeWidth = 3f
            strokePaint.color = 0xFFDF9D3E.toInt()
            c.drawRect(2f, 2f, w - 2f, h - 2f, strokePaint)
            strokePaint.color = 0xFFEDE8E1.toInt()
            c.drawRect(w * 0.05f, h * 0.05f, w * 0.95f, h * 0.95f, strokePaint)
            strokePaint.color = 0xFF8F867C.toInt()
            c.drawRect(w * 0.10f, h * 0.10f, w * 0.90f, h * 0.90f, strokePaint)
            // Center circle (verifies 1:1 aspect ratio)
            strokePaint.color = 0xFFDF9D3E.toInt()
            c.drawCircle(w / 2f, h / 2f, h * 0.34f, strokePaint)
        }

        private fun drawSharpnessChart(c: Canvas, w: Float, h: Float) {
            c.drawColor(0xFF12100E.toInt())
            val sizes = floatArrayOf(72f, 52f, 38f, 28f, 22f, 16f)
            var y = 130f
            for (sz in sizes) {
                displayPaint.textSize = sz
                c.drawText("VELOTV · 1234567890 · Nitidez ${sz.toInt()}px", 96f, y, displayPaint)
                y += sz + 40f
            }
            // Fine 1px alternating lines box on right
            strokePaint.strokeWidth = 1f
            strokePaint.color = 0xFFEDE8E1.toInt()
            var x = w * 0.68f
            while (x < w * 0.90f) {
                c.drawLine(x, 120f, x, h - 180f, strokePaint)
                x += 4f
            }
        }

        private fun drawHud(c: Canvas, w: Float, h: Float) {
            fillPaint.color = 0xFF12100E.toInt()
            c.drawRect(48f, h - 106f, w - 48f, h - 36f, fillPaint)
            strokePaint.color = 0xFF332E29.toInt()
            strokePaint.strokeWidth = 2f
            c.drawRect(48f, h - 106f, w - 48f, h - 36f, strokePaint)
            textPaint.color = 0xFFEDE8E1.toInt()
            textPaint.textSize = 24f
            c.drawText(patternNames[currentPattern], 76f, h - 63f, textPaint)
            textPaint.color = 0xFFDF9D3E.toInt()
            val hint = "◀▶ Cambiar patrón   ·   OK Ocultar barra   ·   Atrás Salir"
            val hw = textPaint.measureText(hint)
            c.drawText(hint, w - 76f - hw, h - 63f, textPaint)
        }
    }
}

