package io.github.mylnzon58.velotv.data.inputs

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import io.github.mylnzon58.velotv.domain.model.TvInputSource
import io.github.mylnzon58.velotv.domain.model.TvInputType
import io.github.mylnzon58.velotv.domain.repository.TvInputRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidTvInputRepository(private val context: Context) : TvInputRepository {

    override suspend fun loadTvInputs(): List<TvInputSource> = withContext(Dispatchers.IO) {
        val tvInputManager = context.getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager
            ?: return@withContext emptyList()

        val rawList = runCatching { tvInputManager.tvInputList }.getOrDefault(emptyList())
        val seenLabels = HashSet<String>()
        val results = mutableListOf<TvInputSource>()

        for (info in rawList) {
            if (info.isHidden(context)) continue
            val type = mapType(info)
            val isSystemService = (info.serviceInfo?.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) ?: 0) != 0
            if (!info.isPassthroughInput && !(type == TvInputType.TUNER && isSystemService)) continue

            val label = resolveFriendlyLabel(info, type)
            val dedupeKey = "${type.name}:$label"
            if (!seenLabels.add(dedupeKey)) continue

            val state = runCatching { tvInputManager.getInputState(info.id) }
                .getOrDefault(TvInputManager.INPUT_STATE_CONNECTED)

            results += TvInputSource(
                id = info.id,
                label = label,
                defaultLabel = label,
                type = type,
                isConnected = state == TvInputManager.INPUT_STATE_CONNECTED,
                isPassthrough = info.isPassthroughInput
            )
        }

        results.sortedWith(
            compareBy<TvInputSource> {
                when (it.type) {
                    TvInputType.HDMI -> 0
                    TvInputType.TUNER -> 1
                    TvInputType.AV -> 2
                    TvInputType.OTHER -> 3
                }
            }.thenBy { it.label }
        )
    }

    override fun launchTvInput(input: TvInputSource): Boolean {
        val uri = if (input.isPassthrough) {
            TvContract.buildChannelUriForPassthroughInput(input.id)
        } else {
            TvContract.Channels.CONTENT_URI
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun mapType(info: TvInputInfo): TvInputType = when (info.type) {
        TvInputInfo.TYPE_HDMI -> TvInputType.HDMI
        TvInputInfo.TYPE_TUNER -> TvInputType.TUNER
        TvInputInfo.TYPE_COMPOSITE,
        TvInputInfo.TYPE_COMPONENT,
        TvInputInfo.TYPE_SVIDEO,
        TvInputInfo.TYPE_SCART -> TvInputType.AV
        else -> TvInputType.OTHER
    }

    private fun resolveFriendlyLabel(info: TvInputInfo, type: TvInputType): String {
        val custom = runCatching { info.loadCustomLabel(context)?.toString()?.trim() }.getOrNull()
        if (!custom.isNullOrEmpty()) return custom
        val label = runCatching { info.loadLabel(context)?.toString()?.trim() }.getOrNull()
        if (!label.isNullOrEmpty()) return label
        return when (type) {
            TvInputType.HDMI -> "HDMI"
            TvInputType.TUNER -> "TV"
            TvInputType.AV -> "AV"
            TvInputType.OTHER -> "Entrada"
        }
    }
}
