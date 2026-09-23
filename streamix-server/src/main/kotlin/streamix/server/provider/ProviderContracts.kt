package streamix.server.provider

import streamix.server.model.*

interface AnimeProvider {
    val id: String
    suspend fun search(query: String, page: Int = 1): List<ProviderAnime>
    suspend fun detail(anime: ProviderAnime): ProviderAnime = anime
    suspend fun episodes(anime: ProviderAnime): List<EpisodeRef>
    suspend fun sources(episode: EpisodeRef): List<SourceRef>
}

class ProviderRegistry(providers: Iterable<AnimeProvider>) {
    private val byId = providers.associateBy { it.id.trim().lowercase() }

    fun get(providerId: String): AnimeProvider? =
        byId[providerId.trim().lowercase()]

    fun all(): List<AnimeProvider> = byId.values.toList()
}
