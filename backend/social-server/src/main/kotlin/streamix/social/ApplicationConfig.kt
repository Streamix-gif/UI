package streamix.social

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

fun Application.configureSocialServer(){
    val db=Database(System.getenv("DATABASE_URL")?:error("DATABASE_URL is required"),System.getenv("DATABASE_USER")?:"postgres",System.getenv("DATABASE_PASSWORD")?:error("DATABASE_PASSWORD is required"))
    val google=GoogleIdentityVerifier(System.getenv("GOOGLE_WEB_CLIENT_ID")?:error("GOOGLE_WEB_CLIENT_ID is required"))
    val sessions=SessionTokens(System.getenv("ANILAB_SESSION_SECRET")?:error("ANILAB_SESSION_SECRET is required"))
    val repo=SocialRepository(db)
    val hub=SocialHub()

    install(CallLogging)
    install(ContentNegotiation){json(Json{ignoreUnknownKeys=true;encodeDefaults=true})}
    install(StatusPages){
        exception<IllegalArgumentException>{call,cause->call.respond(HttpStatusCode.BadRequest,ErrorDto(cause.message?:"Bad request"))}
        exception<Throwable>{call,cause->application.log.error("Unhandled backend error",cause);call.respond(HttpStatusCode.InternalServerError,ErrorDto("Internal server error"))}
    }
    install(WebSockets){pingPeriod=20.seconds;timeout=30.seconds;maxFrameSize=65536L}
    db.migrate()
    installSessionAuth(sessions)
    environment.monitor.subscribe(ApplicationStopped){db.close()}

    routing{
        get("/health"){call.respond(mapOf("status" to "ok","service" to "streamix-social"))}
        post("/auth/google"){
            val i=google.verify(call.receive<GoogleAuthRequest>().idToken)
            val u=repo.upsertUser(i)
            call.respond(AuthResponse(sessions.issue(u.uid,u.email,u.nickname),u))
        }
        get("/users/me"){val p=call.sessionPrincipal();repo.touch(p.uid);call.respond(repo.profile(p.uid)?:ErrorDto("User not found"))}
        get("/social/dashboard"){val p=call.sessionPrincipal();repo.touch(p.uid);call.respond(SocialDashboardDto(repo.friends(p.uid),repo.leaderboard(),repo.recentMessages("global",null),repo.activity(p.uid),repo.activeFriends(p.uid)))}
        get("/social/friends"){call.respond(repo.friends(call.sessionPrincipal().uid))}
        post("/social/friends/{uid}"){val p=call.sessionPrincipal();repo.requestFriend(p.uid,call.parameters["uid"]!!);call.respond(HttpStatusCode.Accepted)}
        post("/social/friends/{uid}/accept"){val p=call.sessionPrincipal();repo.acceptFriend(p.uid,call.parameters["uid"]!!);call.respond(HttpStatusCode.NoContent)}
        get("/social/chat"){call.respond(repo.recentMessages(call.request.queryParameters["roomType"]?:"global",call.request.queryParameters["roomId"]))}
        post("/social/chat"){call.respond(HttpStatusCode.Created,repo.addMessage(call.sessionPrincipal().uid,call.receive()))}
        get("/social/leaderboard"){call.respond(repo.leaderboard())}
        get("/social/activity"){call.respond(repo.activity(call.sessionPrincipal().uid))}
        get("/social/friends/active"){call.respond(repo.activeFriends(call.sessionPrincipal().uid))}
        post("/social/watch-together"){call.respond(HttpStatusCode.Created,repo.createWatch(call.sessionPrincipal().uid,call.receive()))}
        post("/social/watch-together/{id}/join"){repo.joinWatch(call.sessionPrincipal().uid,call.parameters["id"]!!);call.respond(HttpStatusCode.NoContent)}
        put("/social/watch-together/{id}"){call.respond(repo.updateWatch(call.sessionPrincipal().uid,call.parameters["id"]!!,call.receive()))}
        webSocket("/ws/social"){
            val p=call.attributes.getOrNull(SessionPrincipalKey)?:run{close(CloseReason(CloseReason.Codes.VIOLATED_POLICY,"Unauthorized"));return@webSocket}
            val room=call.request.queryParameters["room"]?.takeIf{it.isNotBlank()}?:"global"
            hub.join(room,this)
            try{
                for(frame in incoming){
                    if(frame !is Frame.Text)continue
                    val m=runCatching{Json.decodeFromString<SocketMessage>(frame.readText())}.getOrNull()?:continue
                    if(room.startsWith("watch:")&&m.type=="sync"){
                        hub.broadcast(room,Json.encodeToString(SocketEvent("sync",p.uid,positionMs=m.positionMs,state=m.state)));continue
                    }
                    val type=when{room.startsWith("anime:")->"anime";room.startsWith("watch:")->"watch";else->"global"}
                    val roomId=room.substringAfter(':',"").ifBlank{null}
                    val saved=repo.addMessage(p.uid,SendMessageRequest(type,roomId,m.body))
                    hub.broadcast(room,Json.encodeToString(SocketEvent("chat",p.uid,body=saved.body)))
                }
            }finally{hub.leave(room,this)}
        }
    }
}
