package streamix.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import streamix.server.auth.InMemoryUserRepository

fun Application.module() {
    val users = InMemoryUserRepository()

    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "anilab-backend"))
        }

        get("/users/{uid}") {
            val uid = call.parameters["uid"]
            if (uid == null) {
                call.respond(io.ktor.http.HttpStatusCode.BadRequest)
                return@get
            }

            val user = users.find(uid)
            if (user == null) {
                call.respond(io.ktor.http.HttpStatusCode.NotFound)
            } else {
                call.respond(user)
            }
        }
    }
}
