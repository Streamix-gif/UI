package streamix.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = System.getenv("PORT")?.toIntOrNull() ?: 8080, host = "0.0.0.0") {
        configureServer()
    }.start(wait = true)
}
