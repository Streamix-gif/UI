package streamix.provider

import com.lagradost.cloudstream3.AnimeLoadResponse
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvSeriesLoadResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import streamix.api.EpisodeRef
import streamix.api.ProviderAnime
import streamix.api.StreamRef
import java.util.Locale

/**
 * Thin boundary over the native CloudStream MainAPI contract.
 *
 * Search/detail/episode/link extraction stay owned by the provider and
 * CloudStream. Streamix only translates the resulting objects into its
 * app-facing contracts.
 */
interface ProviderRuntime {
    val providerId: String
    suspend fun search(query: String, page: Int = 1): List<ProviderAnime>
    suspend fun loadAnime(providerAnimeId: String): ProviderAnime?
    suspend fun loadEpisodes(providerAnimeId: String): List<EpisodeRef>
    suspend fun loadStreams(episode: EpisodeRef): List<StreamRef>
}

class CloudStreamProviderRuntime(
    private val api: MainAPI
) : ProviderRuntime {
    override val providerId: String =
        api::class.java.simpleName
            .removeSuffix("Provider")
            .replace(Regex("[^a-zA-Z0-9]"), "")
            .lowercase(Locale.ROOT)

    override suspend fun search(query: String, page: Int): List<ProviderAnime> =
        api.search(query, page)?.items?.map { it.toProviderAnime() }.orEmpty()

    override suspend fun loadAnime(providerAnimeId: String): ProviderAnime? =
        api.load(providerAnimeId)?.toProviderAnime()

    override suspend fun loadEpisodes(providerAnimeId: String): List<EpisodeRef> =
        when (val loaded = api.load(providerAnimeId)) {
            is TvSeriesLoadResponse ->
                loaded.episodes.mapIndexed { index, episode ->
                    episode.toEpisodeRef(index)
                }

            is AnimeLoadResponse ->
                loaded.episodes.values
                    .flatten()
                    .mapIndexed { index, episode ->
                        episode.toEpisodeRef(index)
                    }

            else -> emptyList()
        }

    override suspend fun loadStreams(episode: EpisodeRef): List<StreamRef> {
        val links = mutableListOf<ExtractorLink>()
        val subtitles = mutableListOf<SubtitleFile>()

        api.loadLinks(
            data = episode.url,
            isCasting = false,
            subtitleCallback = { subtitles += it },
            callback = { links += it }
        )

        return links.map { link ->
            StreamRef(
                providerId = providerId,
                url = link.url,
                quality = link.quality,
                type = link.type.name,
                headers = link.headers,
                referer = link.referer
            )
        }
    }

    private fun SearchResponse.toProviderAnime() =
        ProviderAnime(
            providerId = providerId,
            id = url,
            title = name,
            url = url
        )

    private fun LoadResponse.toProviderAnime() =
        ProviderAnime(
            providerId = providerId,
            id = url,
            title = name,
            url = url
        )

    private fun Episode.toEpisodeRef(index: Int) =
        EpisodeRef(
            providerId = providerId,
            number = episode ?: (index + 1),
            title = name,
            providerEpisodeId = data,
            url = data
        )
}
