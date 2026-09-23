package streamix.server.resolver

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import streamix.server.extractor.ExtractorContext
import streamix.server.extractor.ExtractorRegistry
import streamix.server.model.SourceRef
import streamix.server.model.StreamRef

fun interface StreamProbe {
    suspend fun isPlayable(stream: StreamRef): Boolean
}

object BasicStreamProbe : StreamProbe {
    override suspend fun isPlayable(stream: StreamRef): Boolean =
        stream.streamUrl.startsWith("http://") || stream.streamUrl.startsWith("https://")
}

class StreamResolver(
    private val extractors: ExtractorRegistry,
    private val probe: StreamProbe = BasicStreamProbe,
) {
    suspend fun resolve(source: SourceRef): List<StreamRef> = coroutineScope {
        extractors.matching(source.url).flatMap { extractor ->
            try {
                extractor.extract(
                    source,
                    ExtractorContext(source.headers, source.referer),
                )
            } catch (_: Throwable) {
                emptyList()
            }
        }.distinctBy { it.streamUrl }.filter { probe.isPlayable(it) }
    }

    suspend fun race(sources: List<SourceRef>): List<StreamRef> = coroutineScope {
        sources.flatMap { source ->
            extractors.matching(source.url).map { extractor ->
                async {
                    try {
                        extractor.extract(
                            source,
                            ExtractorContext(source.headers, source.referer),
                        ).filter { probe.isPlayable(it) }
                    } catch (_: Throwable) {
                        emptyList()
                    }
                }
            }
        }.awaitAll().flatten().distinctBy { it.streamUrl }
    }
}
