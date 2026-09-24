package streamix.auth

/**
 * Authentication boundary for AniLab.
 *
 * Implementations are responsible for verifying the external identity and
 * resolving it to the application's internal user record. UI/profile/social
 * layers depend on this contract rather than on AniList authentication.
 */
interface AniLabAuth {
    suspend fun currentUser(): AniLabUser?
    suspend fun signIn(idToken: String): AniLabUser
    suspend fun signOut()
}
