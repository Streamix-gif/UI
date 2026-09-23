package streamix.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import streamix.server.engine.SaikouStyleEngine
import streamix.server.extractor.ExtractorContext
import streamix.server.extractor.ExtractorRegistry
import streamix.server.extractor.StreamExtractor
import streamix.server.identity.StaticProviderMappingResolver
import streamix.server.model.*
import streamix.server.provider.AnimeProvider
import streamix.server.provider.ProviderRegistry

private class FakeProvider : AnimeProvider {
    override val id = "demo"

    override suspend fun search(query: String, page: Int): List<ProviderAnime> =
        listOf(ProviderAnime(id, "provider-123", "Demo Anime"))

    override suspend fun episodes(anime: ProviderAnime): List<EpisodeRef> =
        listOf(EpisodeRef(id, 1, providerEpisodeId = "ep-1", url = "https://host.example/ep-1"))

    override suspend fun sources(episode: EpisodeRef): List<SourceRef> =
        listOf(SourceRef(id, episode, "https://megaplay.example/embed/ep-1"))
}

private class FakeExtractor : StreamExtractor {
    override val id = "megaplay"

    override fun supports(url: String): Boolean = url.contains("megaplay.example")

    override suspend fun extract(
        source: SourceRef,
        context: ExtractorContext,
    ): List<StreamRef> = listOf(
        StreamRef(
            providerId = source.providerId,
            sourceUrl = source.url,
            streamUrl = "https://cdn.example/video.m3u8",
            quality = 1080,
            type = "hls",
            extractorId = id,
        )
    )
}

class EngineTest {
    @Test
    fun mappingProviderEpisodeAndExtractorReachStream() = runBlocking {
        val engine = SaikouStyleEngine(
            ProviderRegistry(listOf(FakeProvider())),
            ExtractorRegistry(listOf(FakeExtractor())),
            StaticProviderMappingResolver(
                mapOf((21L to "demo") to "provider-123"),
            ),
        )

        val streams = engine.resolveEpisode(
            CanonicalAnimeIdentity(21L, listOf("Demo Anime")),
            episodeNumber = 1,
        )

        assertEquals(1, streams.size)
        assertEquals("megaplay", streams.single().extractorId)
        assertTrue(streams.single().streamUrl.endsWith(".m3u8"))
    }
}
