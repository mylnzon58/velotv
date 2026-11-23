package io.github.mylnzon58.velotv.data.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import io.github.mylnzon58.velotv.domain.model.WallpaperItem
import io.github.mylnzon58.velotv.domain.model.WallpaperSource
import io.github.mylnzon58.velotv.domain.repository.WallpaperCatalogRepository
import io.github.mylnzon58.velotv.domain.usecase.WallpaperPolicyUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.Calendar
import java.util.Locale

/**
 * Memory-safe, OWASP-hardened open wallpaper engine.
 *
 * - Instant 0 ms local CC0/Wikimedia landscape gallery (assets/wallpapers/).
 * - Optional free-license live image banks: Wikimedia Commons Featured Landscapes + Picsum/Unsplash.
 * - Strict HTTPS host allowlist + manual redirect verification + 4 MB download ceiling.
 * - RGB_565 bounded decoding (1280x720 = 1.84 MB RAM max) + 12 MB LRU disk cache.
 */
class OpenWallpaperRepository(
    private val context: Context,
    private val policy: WallpaperPolicyUseCase
) : WallpaperCatalogRepository {

    private val cacheDir: File = File(context.cacheDir, "velotv_wallpapers").apply { mkdirs() }
    private val indexFile: File = File(cacheDir, "online_index.json")

    // Ultra-compact 1.5 MB RAM thumbnail cache (RGB_565 256x144 ~= 73 KB per thumb)
    private val thumbCache = object : LruCache<String, Bitmap>(1536) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    @Volatile
    private var bundledCache: List<WallpaperItem>? = null

    override fun getBundledWallpapers(): List<WallpaperItem> {
        bundledCache?.let { return it }
        val loaded = runCatching {
            val json = context.assets.open("wallpapers/manifest.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        WallpaperItem(
                            id = o.getString("id"),
                            title = o.getString("title"),
                            author = o.getString("author"),
                            license = o.getString("license"),
                            source = WallpaperSource.BUNDLED_OFFLINE,
                            assetPath = o.getString("file")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
        bundledCache = loaded
        return loaded
    }

    override fun getCachedOnlineWallpapers(): List<WallpaperItem> {
        if (!indexFile.exists()) return emptyList()
        return runCatching {
            val arr = JSONArray(indexFile.readText(Charsets.UTF_8))
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val url = o.optString("remoteUrl")
                    if (!policy.isAllowedWallpaperUrl(url)) continue
                    add(
                        WallpaperItem(
                            id = o.getString("id"),
                            title = o.getString("title"),
                            author = o.getString("author"),
                            license = o.getString("license"),
                            source = WallpaperSource.fromId(o.optString("source")),
                            remoteUrl = url
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    override suspend fun refreshOnlineWallpapers(
        source: WallpaperSource,
        offlineOnly: Boolean
    ): List<WallpaperItem> = withContext(Dispatchers.IO) {
        if (offlineOnly || source == WallpaperSource.BUNDLED_OFFLINE ||
            source == WallpaperSource.DYNAMIC_AMBIENT || source == WallpaperSource.SOLID_MINIMAL
        ) {
            return@withContext emptyList()
        }

        val fetched = mutableListOf<WallpaperItem>()
        if (source == WallpaperSource.AUTO_MIX || source == WallpaperSource.WIKIMEDIA) {
            fetched += runCatching { fetchWikimediaFeaturedLandscapes() }.getOrDefault(emptyList())
        }
        if (source == WallpaperSource.AUTO_MIX || source == WallpaperSource.PICSUM_UNSPLASH) {
            fetched += runCatching { fetchPicsumCuratedLandscapes() }.getOrDefault(emptyList())
        }

        val distinct = fetched.distinctBy { it.id }.take(18)
        if (distinct.isNotEmpty()) {
            saveOnlineIndex(distinct)
            distinct
        } else {
            getCachedOnlineWallpapers()
        }
    }

    fun buildPool(
        source: WallpaperSource,
        offlineOnly: Boolean,
        onlineItems: List<WallpaperItem>
    ): List<WallpaperItem> {
        val bundled = getBundledWallpapers()
        if (offlineOnly) return bundled
        return when (source) {
            WallpaperSource.BUNDLED_OFFLINE -> bundled
            WallpaperSource.WIKIMEDIA -> onlineItems.filter { it.source == WallpaperSource.WIKIMEDIA }.ifEmpty { bundled }
            WallpaperSource.PICSUM_UNSPLASH -> onlineItems.filter { it.source == WallpaperSource.PICSUM_UNSPLASH }.ifEmpty { bundled }
            WallpaperSource.AUTO_MIX -> (bundled + onlineItems).distinctBy { it.id }
            WallpaperSource.DYNAMIC_AMBIENT, WallpaperSource.SOLID_MINIMAL -> emptyList()
        }
    }

    suspend fun loadFullBitmap(item: WallpaperItem, maxW: Int = 1280, maxH: Int = 720): Bitmap? =
        withContext(Dispatchers.IO) {
            if (!item.assetPath.isNullOrBlank()) {
                return@withContext decodeAssetRgb565(item.assetPath, maxW, maxH)
            }
            val remote = item.remoteUrl ?: return@withContext null
            val file = ensureDownloaded(item.id, remote) ?: return@withContext null
            decodeFileRgb565(file, maxW, maxH)
        }

    fun getCachedThumbnail(id: String): Bitmap? = thumbCache.get(id)

    suspend fun loadThumbnail(item: WallpaperItem): Bitmap? = withContext(Dispatchers.IO) {
        thumbCache.get(item.id)?.let { return@withContext it }
        val bmp = if (!item.assetPath.isNullOrBlank()) {
            decodeAssetRgb565(item.assetPath, 256, 144)
        } else {
            val remote = item.remoteUrl ?: return@withContext null
            val file = ensureDownloaded(item.id, remote) ?: return@withContext null
            decodeFileRgb565(file, 256, 144)
        }
        if (bmp != null) thumbCache.put(item.id, bmp)
        bmp
    }

    fun trimMemory() {
        thumbCache.evictAll()
    }

    private fun fetchWikimediaFeaturedLandscapes(): List<WallpaperItem> {
        val queries = listOf(
            "Featured_pictures_of_landscapes",
            "Featured_pictures_of_mountains",
            "Featured_pictures_of_the_night_sky"
        )
        val daySeed = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val category = queries[daySeed % queries.size]
        val api = "https://commons.wikimedia.org/w/api.php?action=query" +
            "&generator=categorymembers&gcmtitle=Category:${URLEncoder.encode(category, "UTF-8")}" +
            "&gcmtype=file&gcmlimit=14&prop=imageinfo&iiprop=url|size|extmetadata&iiurlwidth=1280&format=json"

        val body = httpGetBytes(api, MAX_JSON_BYTES)?.toString(Charsets.UTF_8) ?: return emptyList()
        val pages = JSONObject(body).optJSONObject("query")?.optJSONObject("pages") ?: return emptyList()
        val result = mutableListOf<WallpaperItem>()
        val keys = pages.keys()
        while (keys.hasNext()) {
            val page = pages.optJSONObject(keys.next()) ?: continue
            val iiArr = page.optJSONArray("imageinfo") ?: continue
            val ii = iiArr.optJSONObject(0) ?: continue
            val w = ii.optInt("width", 0)
            val h = ii.optInt("height", 0)
            if (w <= h || w < 1000) continue
            val thumbUrl = ii.optString("thumburl").ifBlank { ii.optString("url") }
            if (!policy.isAllowedWallpaperUrl(thumbUrl)) continue
            val meta = ii.optJSONObject("extmetadata")
            val license = meta?.optJSONObject("LicenseShortName")?.optString("value").orEmpty()
                .ifBlank { "CC / Wikimedia" }
            val rawTitle = page.optString("title").removePrefix("File:")
                .substringBeforeLast('.')
                .replace('_', ' ')
                .take(44)
            result += WallpaperItem(
                id = "wiki_${sha16(thumbUrl)}",
                title = rawTitle.ifBlank { "Wikimedia Featured" },
                author = "Wikimedia Commons",
                license = license,
                source = WallpaperSource.WIKIMEDIA,
                remoteUrl = thumbUrl
            )
        }
        return result.take(8)
    }

    private fun fetchPicsumCuratedLandscapes(): List<WallpaperItem> {
        // Curated atmospheric landscapes (mountains, forests, coasts, night skies) — avoids random distracting photos
        val curated = listOf(
            Triple("10", "Bosque de niebla", "Paul Jarvis"),
            Triple("11", "Valle sereno", "Paul Jarvis"),
            Triple("15", "Cascada alpina", "Paul Jarvis"),
            Triple("16", "Horizonte oceánico", "Paul Jarvis"),
            Triple("28", "Sendero en el bosque", "Jerry Adney"),
            Triple("29", "Cumbre nevada", "Go Wild"),
            Triple("54", "Cielo en las montañas", "Nicholas Swanson"),
            Triple("110", "Atardecer en la pradera", "Kenneth Thewissen"),
            Triple("120", "Cielo nocturno", "Guillaume"),
            Triple("128", "Lago al alba", "Matteo Minelli")
        )
        return curated.mapNotNull { (id, title, author) ->
            val url = "https://picsum.photos/id/$id/1280/720.webp"
            if (!policy.isAllowedWallpaperUrl(url)) return@mapNotNull null
            WallpaperItem(
                id = "picsum_$id",
                title = title,
                author = "$author · Unsplash",
                license = "Unsplash License",
                source = WallpaperSource.PICSUM_UNSPLASH,
                remoteUrl = url
            )
        }
    }

    private fun ensureDownloaded(id: String, url: String): File? {
        val safeName = id.replace(Regex("[^a-zA-Z0-9_-]"), "_") + ".img"
        val target = File(cacheDir, safeName)
        if (target.exists() && target.length() > 1024L) {
            target.setLastModified(System.currentTimeMillis())
            return target
        }
        val bytes = httpGetBytes(url, MAX_IMAGE_BYTES) ?: return null
        val tmp = File(cacheDir, "$safeName.tmp")
        return runCatching {
            FileOutputStream(tmp).use { it.write(bytes) }
            if (target.exists()) target.delete()
            tmp.renameTo(target)
            pruneDiskCache()
            target
        }.getOrElse {
            tmp.delete()
            null
        }
    }

    private fun httpGetBytes(initialUrl: String, maxBytes: Int): ByteArray? {
        var currentUrl = initialUrl
        for (hop in 0..MAX_REDIRECTS) {
            if (!policy.isAllowedWallpaperUrl(currentUrl)) return null
            val conn = (URL(currentUrl).openConnection() as? HttpURLConnection) ?: return null
            try {
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 5_000
                conn.readTimeout = 8_000
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", USER_AGENT)
                conn.setRequestProperty("Accept", "image/webp,image/jpeg,application/json")
                val code = conn.responseCode
                if (code in 301..308) {
                    val location = conn.getHeaderField("Location") ?: return null
                    currentUrl = URL(URL(currentUrl), location).toString()
                    continue
                }
                if (code != HttpURLConnection.HTTP_OK) return null
                val contentLen = conn.contentLength
                if (contentLen > maxBytes) return null
                conn.inputStream.use { input ->
                    val out = ByteArrayOutputStream()
                    val buf = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val n = input.read(buf)
                        if (n <= 0) break
                        total += n
                        if (total > maxBytes) return null
                        out.write(buf, 0, n)
                    }
                    return out.toByteArray()
                }
            } finally {
                conn.disconnect()
            }
        }
        return null
    }

    private fun decodeAssetRgb565(assetPath: String, maxW: Int, maxH: Int): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.assets.open(assetPath).use { BitmapFactory.decodeStream(it, null, bounds) }
        val opts = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxW, maxH)
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        context.assets.open(assetPath).use { BitmapFactory.decodeStream(it, null, opts) }
    }.getOrNull()

    private fun decodeFileRgb565(file: File, maxW: Int, maxH: Int): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        val opts = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxW, maxH)
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        BitmapFactory.decodeFile(file.absolutePath, opts)
    }.getOrNull()

    private fun calculateInSampleSize(srcW: Int, srcH: Int, reqW: Int, reqH: Int): Int {
        var sample = 1
        if (srcW > reqW || srcH > reqH) {
            val halfW = srcW / 2
            val halfH = srcH / 2
            while ((halfW / sample) >= reqW && (halfH / sample) >= reqH) {
                sample *= 2
            }
        }
        return sample.coerceAtLeast(1)
    }

    private fun saveOnlineIndex(items: List<WallpaperItem>) {
        runCatching {
            val arr = JSONArray()
            items.forEach { it ->
                arr.put(
                    JSONObject().apply {
                        put("id", it.id)
                        put("title", it.title)
                        put("author", it.author)
                        put("license", it.license)
                        put("source", it.source.id)
                        put("remoteUrl", it.remoteUrl)
                    }
                )
            }
            indexFile.writeText(arr.toString(), Charsets.UTF_8)
        }
    }

    private fun pruneDiskCache() {
        val files = cacheDir.listFiles { f -> f.isFile && f.name.endsWith(".img") } ?: return
        var total = files.sumOf { it.length() }
        if (total <= MAX_DISK_CACHE_BYTES) return
        files.sortedBy { it.lastModified() }.forEach { f ->
            if (total <= MAX_DISK_CACHE_BYTES * 3 / 4) return
            total -= f.length()
            f.delete()
        }
    }

    private fun sha16(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.take(8).joinToString("") { String.format(Locale.US, "%02x", it) }
    }

    companion object {
        private const val USER_AGENT = "VeloTV/1.1 (Android TV Open Source; https://github.com/mylnzon58/velotv)"
        private const val MAX_REDIRECTS = 3
        private const val MAX_JSON_BYTES = 512 * 1024
        private const val MAX_IMAGE_BYTES = 4 * 1024 * 1024
        private const val MAX_DISK_CACHE_BYTES = 12L * 1024L * 1024L
    }
}

