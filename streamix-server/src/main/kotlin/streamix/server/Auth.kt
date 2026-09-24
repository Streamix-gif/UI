package streamix.server

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.time.Duration.Companion.hours

class GoogleIdentityVerifier(private val audience: String) {
    private val verifier by lazy {
        GoogleIdTokenVerifier.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance()
        ).setAudience(listOf(audience)).build()
    }

    fun verify(idToken: String): GoogleIdentity {
        val token: GoogleIdToken = verifier.verify(idToken) ?: error("Invalid Google ID token")
        val payload: Payload = token.payload
        val uid = payload.subject ?: error("Google token has no subject")
        return GoogleIdentity(uid, payload.email, payload["name"] as? String, payload["picture"] as? String)
    }
}

data class GoogleIdentity(val uid: String, val email: String?, val name: String?, val picture: String?)

class SessionTokens(private val secret: String) {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun issue(uid: String, email: String?, nickname: String): String {
        val now = System.currentTimeMillis() / 1000
        val payload = """{"sub":"\${escape(uid)}","email":\${email?.let { "\"\${escape(it)}\"" } ?: "null"},"nickname":"\${escape(nickname)}","iat":$now,"exp":\${now + 24.hours.inWholeSeconds}}"""
        val head = encoder.encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray())
        val body = encoder.encodeToString(payload.toByteArray())
        return head + "." + body + "." + sign(head + "." + body)
    }

    fun verify(token: String): SessionPrincipal {
        val parts = token.split('.')
        require(parts.size == 3) { "Malformed session token" }
        val expected = sign(parts[0] + "." + parts[1])
        require(MessageDigest.isEqual(expected.toByteArray(), parts[2].toByteArray())) { "Invalid session token" }
        val payload = String(decoder.decode(parts[1]), StandardCharsets.UTF_8)
        val uid = Regex("\"sub\":\"([^\"]+)\"").find(payload)?.groupValues?.get(1) ?: error("Missing subject")
        val exp = Regex("\"exp\":(\\d+)").find(payload)?.groupValues?.get(1)?.toLong() ?: error("Missing expiry")
        require(exp > System.currentTimeMillis() / 1000) { "Session expired" }
        return SessionPrincipal(uid)
    }

    private fun sign(input: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return encoder.encodeToString(mac.doFinal(input.toByteArray()))
    }

    private fun escape(value: String) = value.replace("\\", "\\\\").replace("\"", "\\\"")
}

data class SessionPrincipal(val uid: String)
