package io.github.mylnzon58.velotv.ui.tools

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import io.github.mylnzon58.velotv.R
import io.github.mylnzon58.velotv.VeloTvApplication
import io.github.mylnzon58.velotv.domain.model.WallpaperItem
import io.github.mylnzon58.velotv.ui.common.HallmarkSurfaceFactory
import io.github.mylnzon58.velotv.ui.wallpaper.WallpaperBackdropView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Google TV / Chromecast-style Ambient Gallery & OLED Clock screensaver.
 * - Rotates curated free photography smoothly every 45s with slow Ken Burns drift & anti-burn-in shift.
 * - Press OK to toggle Pure OLED Black mode, Left/Right to switch photo, Back to exit smoothly.
 */
class AmbientClockActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var backdrop: WallpaperBackdropView
    private lateinit var clockContainer: LinearLayout
    private lateinit var tvClock: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvPhotoCredit: TextView
    private var shiftStep = 0
    private var use24h = true
    private var oledPureBlack = false
    private var pool: List<WallpaperItem> = emptyList()
    private var photoIndex = 0
    private var loadJob: Job? = null

    private val clockTicker = object : Runnable {
        override fun run() {
            updateTime()
            shiftStep = (shiftStep + 1) % 6
            val offsets = arrayOf(0 to 0, 44 to -22, -38 to 26, 32 to 30, -48 to -16, 18 to -32)
            val (dx, dy) = offsets[shiftStep]
            clockContainer.animate().translationX(dx.toFloat()).translationY(dy.toFloat()).setDuration(1400L).start()
            handler.postDelayed(this, 60_000L)
        }
    }

    private val photoTicker = object : Runnable {
        override fun run() {
            if (!oledPureBlack && pool.isNotEmpty()) {
                photoIndex = (photoIndex + 1) % pool.size
                showCurrentPhoto(animate = true)
            }
            handler.postDelayed(this, 45_000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val container = (application as VeloTvApplication).container
        val settings = container.preferencesRepository.loadSettings()
        use24h = settings.use24HourClock

        val online = container.wallpaperRepository.getCachedOnlineWallpapers()
        pool = container.wallpaperRepository.buildPool(settings.wallpaperSource, settings.wallpaperOfflineOnly, online)
            .ifEmpty { container.wallpaperRepository.getBundledWallpapers() }

        val root = FrameLayout(this).apply {
            setBackgroundColor(0xFF0A0908.toInt())
        }

        backdrop = WallpaperBackdropView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            configureAppearance(
                theme = settings.theme,
                dimPercent = 48,
                kenBurns = true,
                ambientGlowEnabled = false,
                solidOnly = false
            )
        }
        root.addView(backdrop)

        val pad = HallmarkSurfaceFactory.dp(this, 56)
        clockContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.START
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.START
            ).apply {
                marginStart = pad
                bottomMargin = pad
            }
        }

        tvClock = TextView(this).apply {
            typeface = ResourcesCompat.getFont(this@AmbientClockActivity, R.font.outfit_light)
            textSize = 104f
            setTextColor(0xFFF3EFE9.toInt())
            letterSpacing = 0.01f
            setShadowLayer(12f, 0f, 3f, 0xBB000000.toInt())
        }

        tvDate = TextView(this).apply {
            typeface = ResourcesCompat.getFont(this@AmbientClockActivity, R.font.outfit_regular)
            textSize = 20f
            setTextColor(0xFFDCD6CD.toInt())
            letterSpacing = 0.02f
            setShadowLayer(8f, 0f, 2f, 0xBB000000.toInt())
        }

        tvPhotoCredit = TextView(this).apply {
            typeface = ResourcesCompat.getFont(this@AmbientClockActivity, R.font.outfit_regular)
            textSize = 12f
            setTextColor(0xFFB5AEA5.toInt())
            setPadding(0, HallmarkSurfaceFactory.dp(this@AmbientClockActivity, 10), 0, 0)
            setShadowLayer(6f, 0f, 1f, 0xCC000000.toInt())
        }

        clockContainer.addView(tvClock)
        clockContainer.addView(tvDate)
        clockContainer.addView(tvPhotoCredit)
        root.addView(clockContainer)
        setContentView(root)

        root.alpha = 0f
        root.animate().alpha(1f).setDuration(280L).start()
        updateTime()
        showCurrentPhoto(animate = false)
    }

    private fun showCurrentPhoto(animate: Boolean) {
        if (oledPureBlack) {
            backdrop.clearPhoto()
            tvPhotoCredit.text = getString(R.string.ambient_oled_hint)
            return
        }
        val item = pool.getOrNull(photoIndex) ?: return
        tvPhotoCredit.text = "${item.title} · ${item.author}  ·  ${getString(R.string.ambient_photo_hint)}"
        val repo = (application as VeloTvApplication).container.wallpaperRepository
        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            val bmp = repo.loadFullBitmap(item, 1280, 720)
            if (bmp != null) backdrop.transitionTo(bmp, animate = animate)
        }
    }

    override fun onResume() {
        super.onResume()
        backdrop.setPaused(false)
        handler.removeCallbacks(clockTicker)
        handler.removeCallbacks(photoTicker)
        handler.postDelayed(clockTicker, 60_000L)
        handler.postDelayed(photoTicker, 45_000L)
    }

    override fun onPause() {
        super.onPause()
        backdrop.setPaused(true)
        handler.removeCallbacks(clockTicker)
        handler.removeCallbacks(photoTicker)
    }

    override fun onDestroy() {
        loadJob?.cancel()
        backdrop.clearPhoto()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                oledPureBlack = !oledPureBlack
                backdrop.visibility = if (oledPureBlack) View.INVISIBLE else View.VISIBLE
                showCurrentPhoto(animate = true)
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (!oledPureBlack && pool.isNotEmpty()) {
                    photoIndex = (photoIndex + 1) % pool.size
                    showCurrentPhoto(animate = true)
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (!oledPureBlack && pool.isNotEmpty()) {
                    photoIndex = (photoIndex - 1 + pool.size) % pool.size
                    showCurrentPhoto(animate = true)
                }
                true
            }
            else -> {
                finish()
                @Suppress("DEPRECATION")
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                true
            }
        }
    }

    private fun updateTime() {
        val now = Date()
        val pattern = if (use24h) "HH:mm" else "hh:mm a"
        tvClock.text = SimpleDateFormat(pattern, Locale.getDefault()).format(now)
        tvDate.text = SimpleDateFormat("EEEE d 'de' MMMM", Locale.getDefault()).format(now)
    }
}
