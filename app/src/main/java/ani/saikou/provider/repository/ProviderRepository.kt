package ani.saikou.provider.repository

import ani.saikou.backend.BackendResult
import ani.saikou.provider.model.CanonicalAnimeIdentity
import ani.saikou.provider.model.ProviderAnime
import ani.saikou.provider.model.ProviderEpisode
import ani.saikou.provider.model.ProviderStream

/**
 * Provider boundary. Implementations stay independent from Social and the player.
 */
interface ProviderRepository {
    suspend fun search(query: String): BackendResult<List<ProviderAnime>>
    suspend fun getAnime(identity: CanonicalAnimeIdentity): BackendResult<ProviderAnime>
    suspend fun getEpisodes(anime: ProviderAnime): BackendResult<List<ProviderEpisode>>
    suspend fun getStreams(episode: ProviderEpisode): BackendResult<List<ProviderStream>>
}
