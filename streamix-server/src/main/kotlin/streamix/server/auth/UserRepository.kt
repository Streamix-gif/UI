package streamix.server.auth

interface UserRepository {
    suspend fun find(uid: String): AniLabUser?
    suspend fun createOrUpdateIdentity(
        uid: String,
        email: String?,
        defaultNickname: String
    ): AniLabUser
    suspend fun updateProfile(
        uid: String,
        nickname: String,
        avatarUrl: String?,
        bio: String?
    ): AniLabUser
}

class InMemoryUserRepository : UserRepository {
    private val users = java.util.concurrent.ConcurrentHashMap<String, AniLabUser>()

    override suspend fun find(uid: String): AniLabUser? = users[uid]

    override suspend fun createOrUpdateIdentity(
        uid: String,
        email: String?,
        defaultNickname: String
    ): AniLabUser {
        require(uid.isNotBlank())
        val now = System.currentTimeMillis()
        return users.compute(uid) { _, old ->
            old?.copy(
                email = email ?: old.email,
                updatedAtEpochMillis = now
            ) ?: AniLabUser(
                uid = uid,
                email = email,
                nickname = defaultNickname.ifBlank { email?.substringBefore("@").orEmpty() }.ifBlank { "User" },
                avatarUrl = null,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        }!!
    }

    override suspend fun updateProfile(
        uid: String,
        nickname: String,
        avatarUrl: String?,
        bio: String?
    ): AniLabUser {
        require(nickname.isNotBlank())
        val now = System.currentTimeMillis()
        return users.compute(uid) { _, old ->
            requireNotNull(old) { "User does not exist: $uid" }
            old.copy(
                nickname = nickname.trim(),
                avatarUrl = avatarUrl,
                bio = bio,
                updatedAtEpochMillis = now
            )
        }!!
    }
}
