package streamix.server.model

data class CanonicalAnimeIdentity(
    val anilistId: Long,
    val titles: List<String> = emptyList(),
    val providerMappings: Map<String, String> = emptyMap(),
) {
    init {
        require(anilistId > 0L)
    }

    fun mapping(providerId: String): String? =
        providerMappings[providerId.trim().lowercase()]
}

data class ProviderAnime(
    val providerId: String,
    val id: String,
    val title: String,
    val url: String = id,
)

data class EpisodeRef(
    val providerId: String,
    val number: Int,
    val title: String? = null,
    val providerEpisodeId: String,
    val url: String,
)

data class SourceRef(
    val providerId: String,
    val episode: EpisodeRef,
    val url: String,
    val quality: Int? = null,
    val server: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val referer: String? = null,
)

data class StreamRef(
    val providerId: String,
    val sourceUrl: String,
    val streamUrl: String,
    val quality: Int? = null,
    val type: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val referer: String? = null,
    val extractorId: String? = null,
)
