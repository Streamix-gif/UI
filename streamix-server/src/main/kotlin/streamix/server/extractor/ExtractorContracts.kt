package streamix.server.extractor

import streamix.server.model.SourceRef
import streamix.server.model.StreamRef

data class ExtractorContext(
    val headers: Map<String, String> = emptyMap(),
    val referer: String? = null,
)

interface StreamExtractor {
    val id: String
    fun supports(url: String): Boolean
    suspend fun extract(
        source: SourceRef,
        context: ExtractorContext = ExtractorContext(),
    ): List<StreamRef>
}

class ExtractorRegistry(extractors: Iterable<StreamExtractor>) {
    private val entries = extractors.toList()

    fun matching(url: String): List<StreamExtractor> =
        entries.filter { it.supports(url) }

    fun all(): List<StreamExtractor> = entries
}
