package streamix.api

/**
 * Provider-owned search/detail result.
 *
 * This is intentionally separate from CanonicalAnimeIdentity: a provider URL
 * is not an AniList identity.
 */
data class ProviderAnime(
    val providerId: String,
    val id: String,
    val title: String,
    val url: String = id
)

data class EpisodeRef(
    val providerId: String,
    val number: Int,
    val title: String? = null,
    val providerEpisodeId: String,
    val url: String
)

data class StreamRef(
    val providerId: String,
    val url: String,
    val quality: Int? = null,
    val type: String? = null,
    val language: String? = null,
    val subtitleLanguage: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val referer: String? = null
)
