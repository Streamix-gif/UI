package streamix.social
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
val SessionPrincipalKey=AttributeKey<SessionPrincipal>("streamix.social.session")
fun Application.installSessionAuth(tokens:SessionTokens){
    intercept(ApplicationCallPipeline.Plugins){
        if(call.request.path()=="/health"||call.request.path()=="/auth/google")return@intercept
        val h=call.request.headers[HttpHeaders.Authorization]?:""
        val token=h.removePrefix("Bearer ").takeIf{h.startsWith("Bearer ")}
        val p=runCatching{token?.let(tokens::verify)}.getOrNull()
        if(p==null){call.respond(HttpStatusCode.Unauthorized,ErrorDto("Unauthorized"));finish()}else call.attributes.put(SessionPrincipalKey,p)
    }
}
fun ApplicationCall.sessionPrincipal():SessionPrincipal=attributes[SessionPrincipalKey]
