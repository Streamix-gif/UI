package streamix.routing

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import streamix.api.CanonicalAnimeIdentity
import streamix.api.EpisodeRef
import streamix.api.ProviderAnime
import streamix.api.StreamRef
import streamix.provider.ProviderRuntime

/**
 * Streamix's only provider-selection layer.
 *
 * The router does not implement provider/extractor behavior. It selects the
 * native provider runtime and delegates the complete CloudStream lifecycle.
 */
class ProviderRouter(
    private val providers: List<ProviderRuntime>,
    private val searchTimeoutMs: Long = 20_000L
) {
    fun provider(providerId: String): ProviderRuntime? =
        providers.firstOrNull { it.providerId.equals(providerId, ignoreCase = true) }

    suspend fun search(query: String, page: Int = 1): List<ProviderAnime> = coroutineScope {
        providers.map { provider ->
            async {
                withTimeoutOrNull(searchTimeoutMs) {
                    runCatching { provider.search(query, page) }.getOrDefault(emptyList())
                }.orEmpty()
            }
        }.awaitAll().flatten()
    }

    suspend fun detail(anime: CanonicalAnimeIdentity): List<ProviderAnime> = coroutineScope {
        anime.providerMappings.mapNotNull { (providerId, providerAnimeId) ->
            provider(providerId)?.let { provider ->
                async { provider.loadAnime(providerAnimeId) }
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun episodes(anime: CanonicalAnimeIdentity): List<EpisodeRef> = coroutineScope {
        anime.providerMappings.mapNotNull { (providerId, providerAnimeId) ->
            provider(providerId)?.let { provider ->
                async { provider.loadEpisodes(providerAnimeId) }
            }
        }.awaitAll().flatten()
            .distinctBy { it.providerId to it.providerEpisodeId }
    }

    suspend fun streams(episode: EpisodeRef): List<StreamRef> =
        provider(episode.providerId)?.loadStreams(episode).orEmpty()
}
