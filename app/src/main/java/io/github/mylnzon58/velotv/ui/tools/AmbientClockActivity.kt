package io.github.mylnzon58.velotv.ui.tools

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import io.github.mylnzon58.velotv.R
import io.github.mylnzon58.velotv.VeloTvApplication
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Minimalist OLED-safe ambient clock screensaver with gentle 60-second anti-burn-in shift.
 */
class AmbientClockActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clockContainer: LinearLayout
    private lateinit var tvClock: TextView
    private lateinit var tvDate: TextView
    private var shiftStep = 0
    private var use24h = true

    private val ticker = object : Runnable {
        override fun run() {
            updateTime()
            shiftStep = (shiftStep + 1) % 6
            val offsets = arrayOf(0 to 0, 48 to -24, -40 to 28, 36 to 34, -52 to -18, 20 to -36)
            val (dx, dy) = offsets[shiftStep]
            clockContainer.animate().translationX(dx.toFloat()).translationY(dy.toFloat()).setDuration(1200L).start()
            handler.postDelayed(this, 60_000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val settings = (application as VeloTvApplication).container.preferencesRepository.loadSettings()
        use24h = settings.use24HourClock

        val root = FrameLayout(this).apply {
            setBackgroundColor(0xFF0A0908.toInt())
        }

        clockContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        }

        tvClock = TextView(this).apply {
            typeface = ResourcesCompat.getFont(this@AmbientClockActivity, R.font.barlow_condensed_light)
            textSize = 112f
            setTextColor(0xFFEDE8E1.toInt())
            letterSpacing = 0.02f
        }

        tvDate = TextView(this).apply {
            typeface = ResourcesCompat.getFont(this@AmbientClockActivity, R.font.barlow_regular)
            textSize = 20f
            setTextColor(0xFF8F867C.toInt())
            letterSpacing = 0.04f
        }

        clockContainer.addView(tvClock)
        clockContainer.addView(tvDate)
        root.addView(clockContainer)
        setContentView(root)
        updateTime()
    }

    override fun onResume() {
        super.onResume()
        handler.removeCallbacks(ticker)
        handler.postDelayed(ticker, 60_000L)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        finish()
        return true
    }

    private fun updateTime() {
        val now = Date()
        val pattern = if (use24h) "HH:mm" else "hh:mm a"
        tvClock.text = SimpleDateFormat(pattern, Locale.getDefault()).format(now)
        tvDate.text = SimpleDateFormat("EEEE d 'de' MMMM", Locale.getDefault()).format(now)
    }
}

