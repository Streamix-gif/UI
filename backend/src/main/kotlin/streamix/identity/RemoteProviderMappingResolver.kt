package streamix.identity

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Saikou-compatible remote mapping resolver.
 *
 * Expected endpoint shape:
 *   /api/anilist/anime/{anilistId}/mappings?provider={providerId}
 *
 * The response is expected to expose the provider ID at:
 *   res.provider.id
 *
 * No title search or fuzzy matching is performed here. A missing/invalid
 * mapping simply returns null so the caller can try another provider.
 */
class RemoteProviderMappingResolver(
    baseUrl: URI,
    private val timeoutMs: Int = 8_000
) : ProviderMappingResolver {

    private val endpointBase = baseUrl.toString().trimEnd('/')
    private val mapper = jacksonObjectMapper()

    override suspend fun resolve(anilistId: Long, providerId: String): String? {
        require(anilistId > 0L) { "anilistId must be positive" }
        if (providerId.isBlank()) return null

        return runCatching {
            val encodedProvider = URLEncoder.encode(
                providerId,
                StandardCharsets.UTF_8.name()
            )
            val url = URI(
                "$endpointBase/api/anilist/anime/$anilistId/mappings?provider=$encodedProvider"
            ).toURL()

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            connection.setRequestProperty("Accept", "application/json")

            if (connection.responseCode !in 200..299) return null

            val root: JsonNode = connection.inputStream.use(mapper::readTree)
            root.path("res")
                .path("provider")
                .path("id")
                .takeIf { !it.isMissingNode && !it.isNull }
                ?.asText()
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
