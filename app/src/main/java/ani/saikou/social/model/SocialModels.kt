package ani.saikou.social.model

import kotlinx.serialization.Serializable

@Serializable
data class SocialUser(
    val id: String,
    val username: String,
    val displayName: String = username,
    val avatar: String? = null,
    val banner: String? = null,
    val bio: String? = null,
    val animeWatched: Int = 0,
    val episodesWatched: Int = 0,
    val favoriteMediaIds: List<Int> = emptyList()
)

@Serializable
enum class RelationshipType {
    FOLLOWING,
    FOLLOWER,
    FRIEND,
    PENDING,
    BLOCKED
}

@Serializable
data class Relationship(
    val userId: String,
    val targetUserId: String,
    val type: RelationshipType
)

@Serializable
data class SocialActivity(
    val id: String,
    val author: SocialUser,
    val type: String,
    val text: String? = null,
    val mediaId: String? = null,
    val createdAt: Long,
    val replyCount: Int = 0
)

@Serializable
data class SocialNotification(
    val id: String,
    val type: String,
    val actor: SocialUser? = null,
    val targetId: String? = null,
    val createdAt: Long,
    val read: Boolean = false
)
