package ani.saikou.provider.model

import kotlinx.serialization.Serializable

@Serializable
data class CanonicalAnimeIdentity(
    val id: String,
    val title: String,
    val cover: String? = null
)

@Serializable
data class ProviderAnime(
    val id: String,
    val providerId: String,
    val identity: CanonicalAnimeIdentity
)

@Serializable
data class ProviderEpisode(
    val id: String,
    val providerId: String,
    val animeId: String,
    val number: Float,
    val title: String? = null,
    val pageUrl: String
)

@Serializable
data class ProviderStream(
    val id: String,
    val providerId: String,
    val episodeId: String,
    val url: String,
    val quality: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val subtitles: List<String> = emptyList()
)
