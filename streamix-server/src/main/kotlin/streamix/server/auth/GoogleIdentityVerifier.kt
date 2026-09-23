package streamix.server.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.apache.v2.ApacheHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import java.util.Collections

data class GoogleIdentity(
    val uid: String,
    val email: String?,
    val nickname: String,
    val avatarUrl: String?
)

class GoogleIdentityVerifier(
    webClientId: String
) {
    private val verifier = GoogleIdTokenVerifier.Builder(
        ApacheHttpTransport(),
        JacksonFactory.getDefaultInstance()
    ).setAudience(Collections.singletonList(webClientId)).build()

    fun verify(idToken: String): GoogleIdentity {
        val token = verifier.verify(idToken)
            ?: throw IllegalArgumentException("Invalid Google ID token")
        val payload = token.payload
        require(payload.emailVerified) { "Google email is not verified" }

        return GoogleIdentity(
            uid = payload.subject,
            email = payload.email,
            nickname = (payload["name"] as? String).orEmpty(),
            avatarUrl = payload["picture"] as? String
        )
    }
}
