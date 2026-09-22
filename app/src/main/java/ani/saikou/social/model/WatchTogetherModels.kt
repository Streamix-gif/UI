package ani.saikou.social.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoomMemberRole {
    OWNER,
    MEMBER
}

@Serializable
enum class PresenceState {
    ONLINE,
    WATCHING,
    PAUSED,
    AWAY,
    OFFLINE
}

@Serializable
data class WatchRoom(
    val id: String,
    val ownerId: String,
    val mediaId: String,
    val episodeId: String? = null,
    val createdAt: Long,
    val active: Boolean = true
)

@Serializable
data class RoomMember(
    val roomId: String,
    val userId: String,
    val role: RoomMemberRole = RoomMemberRole.MEMBER,
    val presence: PresenceState = PresenceState.ONLINE,
    val joinedAt: Long
)

@Serializable
data class PlaybackState(
    val roomId: String,
    val mediaId: String,
    val episodeId: String? = null,
    val positionMs: Long = 0L,
    val playing: Boolean = false,
    val updatedAt: Long
)

@Serializable
data class RoomMessage(
    val id: String,
    val roomId: String,
    val senderId: String,
    val content: String,
    val createdAt: Long
)
