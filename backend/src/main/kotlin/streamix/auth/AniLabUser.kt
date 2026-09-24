package streamix.auth

/**
 * Application-owned identity.
 *
 * AniList IDs must never be used as the primary AniLab account identity.
 * The UID is supplied by the configured authentication provider (Google/Firebase
 * in the Android client) and is the stable key for profile/social data.
 */
data class AniLabUser(
    val uid: String,
    val email: String?,
    val nickname: String,
    val avatarUrl: String?,
    val bio: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
) {
    init {
        require(uid.isNotBlank()) { "uid must not be blank" }
        require(nickname.isNotBlank()) { "nickname must not be blank" }
    }
}
