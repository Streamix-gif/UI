package streamix.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.*

val SessionPrincipalKey = AttributeKey<SessionPrincipal>("streamix.session")

fun Application.installSessionAuth(tokens: SessionTokens) {
    intercept(ApplicationCallPipeline.Plugins) {
        val path = call.request.path()
        if (path == "/health" || path == "/auth/google") return@intercept

        val header = call.request.headers[HttpHeaders.Authorization] ?: ""
        val token = header.removePrefix("Bearer ").takeIf { header.startsWith("Bearer ") }
        val principal = runCatching { token?.let(tokens::verify) }.getOrNull()

        if (principal == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorDto("Unauthorized"))
            finish()
        } else {
            call.attributes.put(SessionPrincipalKey, principal)
        }
    }
}

fun ApplicationCall.sessionPrincipal(): SessionPrincipal = attributes[SessionPrincipalKey]
