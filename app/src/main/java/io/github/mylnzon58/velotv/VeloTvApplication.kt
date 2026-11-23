package io.github.mylnzon58.velotv

import android.app.Application
import io.github.mylnzon58.velotv.core.AppContainer

class VeloTvApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            container.iconCache.clear()
        }
    }
}

