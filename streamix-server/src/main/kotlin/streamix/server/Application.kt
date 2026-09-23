package streamix.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import streamix.server.auth.GoogleIdentityVerifier
import streamix.server.auth.PostgresUserRepository
import streamix.server.auth.UserRepository
import streamix.server.auth.postgresDataSource

@Serializable
data class GoogleLoginRequest(val idToken: String)

@Serializable
data class UpdateProfileRequest(
    val nickname: String,
    val avatarUrl: String? = null,
    val bio: String? = null
)

private fun bearer(call: ApplicationCall): String? =
    call.request.headers["Authorization"]
        ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
        ?.substringAfter(" ")
        ?.trim()
        ?.takeIf { it.isNotBlank() }

fun Application.module(
    users: UserRepository = PostgresUserRepository(postgresDataSource()),
    googleVerifier: GoogleIdentityVerifier = GoogleIdentityVerifier(
        System.getenv("ANILAB_GOOGLE_WEB_CLIENT_ID")
            ?: error("ANILAB_GOOGLE_WEB_CLIENT_ID is required")
    )
) {
    install(ContentNegotiation) { json() }

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "anilab-backend"))
        }

        post("/auth/google") {
            val request = call.receive<GoogleLoginRequest>()
            val identity = runCatching { googleVerifier.verify(request.idToken) }
                .getOrElse {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "invalid_google_token"))
                    return@post
                }

            call.respond(
                users.createOrUpdateIdentity(
                    uid = identity.uid,
                    email = identity.email,
                    defaultNickname = identity.nickname
                )
            )
        }

        get("/users/me") {
            val token = bearer(call)
            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val identity = runCatching { googleVerifier.verify(token) }
                .getOrElse {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

            val user = users.find(identity.uid)
            if (user == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(user)
        }

        post("/users/me/profile") {
            val token = bearer(call)
            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val identity = runCatching { googleVerifier.verify(token) }
                .getOrElse {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

            val request = runCatching { call.receive<UpdateProfileRequest>() }
                .getOrElse {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

            val user = runCatching {
                users.updateProfile(identity.uid, request.nickname, request.avatarUrl, request.bio)
            }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (it.message ?: "profile_update_failed")))
                return@post
            }

            call.respond(user)
        }
    }
}
