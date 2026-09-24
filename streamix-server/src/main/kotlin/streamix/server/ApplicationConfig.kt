package streamix.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

fun Application.configureServer() {
    val db = Database(
        System.getenv("DATABASE_URL") ?: error("DATABASE_URL is required"),
        System.getenv("DATABASE_USER") ?: "postgres",
        System.getenv("DATABASE_PASSWORD") ?: error("DATABASE_PASSWORD is required")
    )
    val googleClientId = System.getenv("GOOGLE_WEB_CLIENT_ID") ?: error("GOOGLE_WEB_CLIENT_ID is required")
    val sessionSecret = System.getenv("ANILAB_SESSION_SECRET") ?: error("ANILAB_SESSION_SECRET is required")
    val repository = SocialRepository(db)
    val auth = GoogleIdentityVerifier(googleClientId)
    val sessions = SessionTokens(sessionSecret)
    val hub = SocialHub()

    install(CallLogging)
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorDto(cause.message ?: "Bad request"))
        }
        exception<Throwable> { call, cause ->
            application.log.error("Unhandled backend error", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorDto("Internal server error"))
        }
    }
    install(WebSockets) {
        pingPeriod = 20.seconds
        timeout = 30.seconds
        maxFrameSize = 64 * 1024
    }

    db.migrate()
    installSessionAuth(sessions)
    environment.monitor.subscribe(ApplicationStopped) { db.close() }

    routing {
        get("/health") { call.respond(mapOf("status" to "ok", "service" to "anilab-backend")) }
        post("/auth/google") {
            val request = call.receive<GoogleAuthRequest>()
            val identity = auth.verify(request.idToken)
            val user = repository.upsertUser(identity)
            call.respond(AuthResponse(sessions.issue(user.uid, user.email, user.nickname), user))
        }
        get("/users/me") {
            val p = call.sessionPrincipal()
            repository.touch(p.uid)
            call.respond(repository.profile(p.uid) ?: ErrorDto("User not found"))
        }
        put("/users/me/profile") {
            val p = call.sessionPrincipal()
            call.respond(repository.updateProfile(p.uid, call.receive()))
        }
        get("/social/dashboard") {
            val p = call.sessionPrincipal()
            repository.touch(p.uid)
            call.respond(SocialDashboardDto(repository.friends(p.uid),repository.leaderboard(),repository.recentMessages("global",null),repository.activity(p.uid),repository.activeFriends(p.uid)))
        }
        get("/social/friends") { call.respond(repository.friends(call.sessionPrincipal().uid)) }
        post("/social/friends/{uid}") {
            val p=call.sessionPrincipal()
            repository.requestFriend(p.uid,call.parameters["uid"]!!)
            call.respond(HttpStatusCode.Accepted)
        }
        post("/social/friends/{uid}/accept") {
            val p=call.sessionPrincipal()
            repository.acceptFriend(p.uid,call.parameters["uid"]!!)
            call.respond(HttpStatusCode.NoContent)
        }
        get("/social/chat") {
            val type=call.request.queryParameters["roomType"]?:"global"
            call.respond(repository.recentMessages(type,call.request.queryParameters["roomId"]))
        }
        post("/social/chat") {
            val message=repository.addMessage(call.sessionPrincipal().uid,call.receive())
            call.respond(HttpStatusCode.Created,message)
        }
        get("/social/leaderboard") { call.respond(repository.leaderboard()) }
        get("/social/activity") { call.respond(repository.activity(call.sessionPrincipal().uid)) }
        get("/social/friends/active") { call.respond(repository.activeFriends(call.sessionPrincipal().uid)) }
        post("/social/watch-together") {
            call.respond(HttpStatusCode.Created,repository.createWatchRoom(call.sessionPrincipal().uid,call.receive()))
        }
        post("/social/watch-together/{id}/join") {
            repository.joinWatchRoom(call.sessionPrincipal().uid,call.parameters["id"]!!)
            call.respond(HttpStatusCode.NoContent)
        }
        put("/social/watch-together/{id}") {
            call.respond(repository.updateWatchRoom(call.sessionPrincipal().uid,call.parameters["id"]!!,call.receive()))
        }
        webSocket("/ws/social") {
            val principal=call.attributes.getOrNull(SessionPrincipalKey)?:run{
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY,"Unauthorized"))
                return@webSocket
            }
            val room=call.request.queryParameters["room"]?.takeIf(String::isNotBlank)?:"global"
            hub.join(room,this)
            try {
                for(frame in incoming) {
                    if(frame !is Frame.Text) continue
                    val request=runCatching{Json.decodeFromString<SocketMessage>(frame.readText())}.getOrNull()?:continue
                    if(room.startsWith("watch:")&&request.type=="sync"){
                        hub.broadcast(room,Json.encodeToString(SocketEvent("sync",principal.uid,positionMs=request.positionMs,state=request.state)))
                        continue
                    }
                    val type=when{
                        room.startsWith("anime:")->"anime"
                        room.startsWith("watch:")->"watch"
                        else->"global"
                    }
                    val roomId=room.substringAfter(':',"").ifBlank{null}
                    val message=repository.addMessage(principal.uid,SendMessageRequest(type,roomId,request.body))
                    hub.broadcast(room,Json.encodeToString(SocketEvent("chat",principal.uid,body=message.body)))
                }
            } finally { hub.leave(room,this) }
        }
    }
}
