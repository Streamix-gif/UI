package ani.saikou.social.model

import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: String,
    val participantIds: List<String>,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0
)

@Serializable
data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val replyTo: String? = null,
    val createdAt: Long,
    val editedAt: Long? = null
)

@Serializable
data class ChatRoom(
    val id: String,
    val type: ChatRoomType,
    val title: String? = null,
    val mediaId: String? = null,
    val episodeId: String? = null
)

@Serializable
enum class ChatRoomType {
    GLOBAL,
    ANIME,
    EPISODE,
    WATCH_ROOM
}

@Serializable
data class ChatMessage(
    val id: String,
    val roomId: String,
    val sender: SocialUser,
    val content: String,
    val replyTo: String? = null,
    val createdAt: Long
)
