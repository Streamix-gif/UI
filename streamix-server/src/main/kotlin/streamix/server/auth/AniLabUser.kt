package streamix.server.auth

import kotlinx.serialization.Serializable

@Serializable
data class AniLabUser(
    val uid: String,
    val email: String?,
    val nickname: String,
    val avatarUrl: String?,
    val bio: String? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
