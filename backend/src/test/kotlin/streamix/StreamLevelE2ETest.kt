package streamix

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import streamix.routing.ProviderRouter
import streamix.runtime.CloudStreamHost
import java.net.HttpURLConnection
import java.net.URI

@RunWith(RobolectricTestRunner::class)
class StreamLevelE2ETest {
    @Test(timeout = 120_000)
    fun otakudesuSearchToPlayableStreamRunsWithoutAndroidDevice() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        val provider = CloudStreamHost.runtimes(context)
            .firstOrNull { it.providerId.equals("otakudesu", ignoreCase = true) }
            ?: error("Otakudesu provider was not registered")

        val router = ProviderRouter(listOf(provider))
        val results = router.search("One Piece")
        assertFalse("Otakudesu search returned no anime", results.isEmpty())

        val anime = results.firstOrNull { it.providerId.equals(provider.providerId, true) }
            ?: error("Otakudesu search returned no provider result")

        val episodes = runCatching { provider.loadEpisodes(anime.id) }.getOrElse { error ->
            println("=== OTAKUDESU DETAIL DIAGNOSTIC ===")
            println("provider=${provider.providerId}")
            println("animeId=${anime.id}")
            println("animeTitle=${anime.title}")
            diagnosticHttp(anime.id)
            throw error
        }
        assertFalse("Otakudesu returned no episodes for ${anime.title}", episodes.isEmpty())

        val episode = episodes.firstOrNull { it.number == 1 }
            ?: error("Otakudesu did not return episode 1 for ${anime.title}")

        val streams = router.streams(episode)
        assertFalse(
            "Otakudesu loadLinks returned no stream candidates for ${anime.title} episode ${episode.number}",
            streams.isEmpty()
        )

        val playable = streams.firstOrNull { probeStream(it.url, it.headers, it.referer) }
            ?: error("Otakudesu returned stream candidates, but none passed the playable-stream probe")

        assertTrue("Playable stream URL is blank", playable.url.isNotBlank())
    }

    private fun diagnosticHttp(url: String) {
        runCatching {
            val connection = (URI(url).toURL().openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 15_000
                instanceFollowRedirects = true
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"
                )
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            }
            connection.connect()
            val bytes = connection.inputStream.use { it.readNBytes(8192) }
            val body = bytes.toString(Charsets.UTF_8)
            println("httpStatus=${connection.responseCode}")
            println("finalUrl=${connection.url}")
            println("contentType=${connection.contentType}")
            println("contentLength=${connection.contentLengthLong}")
            println("bodyPrefix=${body.take(1000).replace(Regex("\\s+"), " ")}")
            println("hasInfozingle=${body.contains("infozingle", ignoreCase = true)}")
            println("hasEpisodeList=${body.contains("episodelist", ignoreCase = true)}")
            connection.disconnect()
        }.onFailure {
            println("diagnosticHttpError=${it.javaClass.name}: ${it.message}")
        }
    }

    private fun probeStream(url: String, headers: Map<String, String>, referer: String?): Boolean {
        if (url.isBlank()) return false
        val connection = runCatching {
            (URI(url).toURL().openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 15_000
                instanceFollowRedirects = true
                headers.forEach { (name, value) -> setRequestProperty(name, value) }
                if (!referer.isNullOrBlank()) setRequestProperty("Referer", referer)
                setRequestProperty("Range", "bytes=0-4095")
            }
        }.getOrNull() ?: return false

        return runCatching {
            connection.connect()
            if (connection.responseCode !in 200..299) return@runCatching false
            val contentType = connection.contentType.orEmpty().lowercase()
            val bytes = connection.inputStream.use { it.readNBytes(4096) }
            val prefix = bytes.toString(Charsets.UTF_8)
            val looksLikeHls = contentType.contains("mpegurl") || url.contains(".m3u8", true)
            val looksLikeMedia = contentType.startsWith("video/") ||
                contentType.startsWith("audio/") ||
                url.substringBefore("?").matches(
                    Regex(".*\\.(mp4|mkv|webm|m4v|ts|m3u8)$", RegexOption.IGNORE_CASE)
                )
            (looksLikeHls && prefix.contains("#EXTM3U")) || looksLikeMedia
        }.getOrDefault(false).also { connection.disconnect() }
    }
}
