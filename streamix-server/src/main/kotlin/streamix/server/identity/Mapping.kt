package streamix.server.identity

import streamix.server.model.CanonicalAnimeIdentity

fun interface ProviderMappingResolver {
    suspend fun resolve(anilistId: Long, providerId: String): String?
}

class StaticProviderMappingResolver(
    private val mappings: Map<Pair<Long, String>, String>,
) : ProviderMappingResolver {
    override suspend fun resolve(anilistId: Long, providerId: String): String? =
        mappings[anilistId to providerId.trim().lowercase()]
}

suspend fun CanonicalAnimeIdentity.withResolvedMappings(
    providerIds: Iterable<String>,
    resolver: ProviderMappingResolver,
): CanonicalAnimeIdentity {
    val resolved = providerIds.associateWith { providerId ->
        resolver.resolve(anilistId, providerId)
    }.filterValues { !it.isNullOrBlank() }

    return copy(
        providerMappings = providerMappings + resolved.mapKeys { it.key.trim().lowercase() }
            .mapValues { it.value!! },
    )
}
