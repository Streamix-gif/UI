package ani.saikou.backend

import kotlinx.serialization.Serializable

/**
 * Authentication identity is separate from the editable app profile.
 * The concrete provider (Google, email, etc.) is intentionally not coupled here.
 */
@Serializable
data class AuthIdentity(
    val id: String,
    val provider: String,
    val email: String? = null
)

@Serializable
data class AuthSession(
    val accessToken: String,
    val identity: AuthIdentity,
    val expiresAt: Long? = null
)
