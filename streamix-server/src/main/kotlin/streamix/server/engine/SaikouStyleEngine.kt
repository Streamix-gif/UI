package streamix.server.engine

import streamix.server.extractor.ExtractorRegistry
import streamix.server.identity.ProviderMappingResolver
import streamix.server.identity.withResolvedMappings
import streamix.server.model.CanonicalAnimeIdentity
import streamix.server.model.ProviderAnime
import streamix.server.model.StreamRef
import streamix.server.provider.ProviderRegistry
import streamix.server.resolver.StreamResolver

class SaikouStyleEngine(
    private val providers: ProviderRegistry,
    private val extractorRegistry: ExtractorRegistry,
    private val mappingResolver: ProviderMappingResolver,
    private val streamResolver: StreamResolver = StreamResolver(extractorRegistry),
) {
    suspend fun search(query: String, page: Int = 1): List<ProviderAnime> =
        providers.all().flatMap { it.search(query, page) }

    suspend fun resolveEpisode(
        identity: CanonicalAnimeIdentity,
        episodeNumber: Int,
        providerIds: Iterable<String> = providers.all().map { it.id },
    ): List<StreamRef> {
        val ids = providerIds.toList()
        val mapped = identity.withResolvedMappings(ids, mappingResolver)

        return ids.flatMap { providerId ->
            val provider = providers.get(providerId) ?: return@flatMap emptyList()
            val mappedId = mapped.mapping(providerId) ?: return@flatMap emptyList()

            val anime = provider.search(mappedId, 1).firstOrNull { it.id == mappedId }
                ?: ProviderAnime(
                    provider.id,
                    mappedId,
                    mapped.titles.firstOrNull() ?: mappedId,
                    mappedId,
                )

            val episode = provider.episodes(anime).firstOrNull { it.number == episodeNumber }
                ?: return@flatMap emptyList()

            streamResolver.race(provider.sources(episode))
        }
    }

    fun extractorIds(): List<String> = extractorRegistry.all().map { it.id }
}
