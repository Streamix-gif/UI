package ani.dantotsu.parsers

import android.content.Context
import android.util.Base64
import ani.dantotsu.currContext
import streamix.api.EpisodeRef
import streamix.api.ProviderAnime
import streamix.api.StreamRef
import streamix.runtime.DefaultRuntime
import streamix.runtime.StreamixService

class StreamixAnimeParser : AnimeParser() {
    override val name = "Streamix"
    override val saveName = "Streamix"
    override val hostUrl = "streamix://backend"

    private val service: StreamixService
        get() = StreamixProviderBridge.service(currContext() ?: error("Context unavailable"))

    override suspend fun search(query: String): List<ShowResponse> = service.search(query).map { it.toShowResponse() }

    override suspend fun loadEpisodes(animeLink: String, extra: Map<String, String>?, sAnime: eu.kanade.tachiyomi.animesource.model.SAnime): List<Episode> {
        val ref = decodeAnime(animeLink)
        return service.providerEpisodes(ref.providerId, ref.id).map { it.toEpisode() }
    }

    override suspend fun loadVideoServers(episodeLink: String, extra: Map<String, String>?, sEpisode: eu.kanade.tachiyomi.animesource.model.SEpisode): List<VideoServer> {
        val episode = decodeEpisode(episodeLink)
        return service.providerStreams(episode).mapIndexed { index, stream -> VideoServer(streamName(stream, index), encodeStream(stream)) }
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? = StreamixVideoExtractor(server, decodeStream(server.embed.url))

    private fun ProviderAnime.toShowResponse() = ShowResponse(name = title, link = encodeAnime(providerId, id), coverUrl = defaultImage, sAnime = eu.kanade.tachiyomi.animesource.model.SAnime.create().apply {
        title = this@toShowResponse.title
        url = encodeAnime(providerId, id)
        thumbnail_url = defaultImage
    })

    private fun EpisodeRef.toEpisode() = Episode(number = number.toString(), link = encodeEpisode(providerId, providerEpisodeId, url), title = title, sEpisode = eu.kanade.tachiyomi.animesource.model.SEpisode.create().apply {
        name = title ?: "Episode " + number
        episode_number = number.toFloat()
        url = encodeEpisode(providerId, providerEpisodeId, url)
    })

    private fun encodeAnime(providerId: String, id: String) = pack("A|" + providerId + "|" + id)
    private fun encodeEpisode(providerId: String, episodeId: String, url: String) = pack("E|" + providerId + "|" + episodeId + "|" + url)
    private fun encodeStream(stream: StreamRef) = pack("S|" + stream.providerId + "|" + stream.url + "|" + (stream.quality ?: -1) + "|" + (stream.type ?: ""))
    private fun pack(value: String) = Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    private fun unpack(value: String) = String(Base64.decode(value, Base64.NO_WRAP), Charsets.UTF_8)

    private fun decodeAnime(value: String): ProviderRef {
        val parts = unpack(value).split("|", limit = 3)
        require(parts.size == 3 && parts[0] == "A")
        return ProviderRef(parts[1], parts[2])
    }
    private fun decodeEpisode(value: String): EpisodeRef {
        val parts = unpack(value).split("|", limit = 4)
        require(parts.size == 4 && parts[0] == "E")
        return EpisodeRef(parts[1], 0, null, parts[2], parts[3])
    }
    private fun decodeStream(value: String): StreamRef {
        val parts = unpack(value).split("|", limit = 5)
        require(parts.size == 5 && parts[0] == "S")
        return StreamRef(providerId = parts[1], url = parts[2], quality = parts[3].toIntOrNull()?.takeUnless { it < 0 }, type = parts[4].ifBlank { null })
    }
    private fun streamName(stream: StreamRef, index: Int) = stream.quality?.takeIf { it > 0 }?.let { it.toString() + "p" } ?: ("Stream " + (index + 1))
}

private data class ProviderRef(val providerId: String, val id: String)

private object StreamixProviderBridge {
    @Volatile private var instance: StreamixService? = null
    fun service(context: Context): StreamixService = instance ?: synchronized(this) { instance ?: DefaultRuntime.create(context.applicationContext).also { instance = it } }
}

private class StreamixVideoExtractor(override val server: VideoServer, private val stream: StreamRef) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        val type = when (stream.type?.lowercase()) {
            "m3u8", "application/vnd.apple.mpegurl" -> VideoType.M3U8
            "dash", "application/dash+xml" -> VideoType.DASH
            else -> VideoType.CONTAINER
        }
        return VideoContainer(videos = listOf(Video(quality = stream.quality, videoType = type, url = stream.url, size = null)))
    }
}