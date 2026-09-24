package streamix.auth

/**
 * Application-owned profile operations.
 */
interface AniLabProfile {
    suspend fun get(uid: String): AniLabUser?
    suspend fun update(
        uid: String,
        nickname: String,
        avatarUrl: String?,
        bio: String?
    ): AniLabUser
}
