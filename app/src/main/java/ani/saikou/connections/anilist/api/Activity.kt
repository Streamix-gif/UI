package ani.saikou.connections.anilist.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    @SerialName("id") val id: Int,
    @SerialName("__typename") val typename: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("userId") val userId: Int? = null,
    @SerialName("replyCount") val replyCount: Int = 0,
    @SerialName("recipientId") val recipientId: Int? = null,
    @SerialName("messengerId") val messengerId: Int? = null,
    @SerialName("user") val user: User? = null,
    @SerialName("recipient") val recipient: User? = null,
    @SerialName("messenger") val messenger: User? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("progress") val progress: String? = null,
    @SerialName("media") val media: ActivityMedia? = null,
    @SerialName("createdAt") val createdAt: Long? = null,
    @SerialName("likeCount") val likeCount: Int? = null,
    @SerialName("isLiked") val isLiked: Boolean? = null,
    @SerialName("isLocked") val isLocked: Boolean? = null,
    @SerialName("isSubscribed") val isSubscribed: Boolean? = null,
    @SerialName("isPinned") val isPinned: Boolean? = null,
    @SerialName("isPrivate") val isPrivate: Boolean? = null,
    @SerialName("siteUrl") val siteUrl: String? = null,
    @SerialName("replies") val replies: List<ActivityReply>? = null
)

@Serializable
data class ActivityMedia(
    @SerialName("title") val title: ActivityMediaTitle? = null
)

@Serializable
data class ActivityMediaTitle(
    @SerialName("english") val english: String? = null,
    @SerialName("romaji") val romaji: String? = null,
    @SerialName("userPreferred") val userPreferred: String? = null
)

@Serializable
data class ActivityReply(
    @SerialName("id") val id: Int,
    @SerialName("userId") val userId: Int,
    @SerialName("activityId") val activityId: Int? = null,
    @SerialName("text") val text: String,
    @SerialName("likeCount") var likeCount: Int = 0,
    @SerialName("isLiked") var isLiked: Boolean = false,
    @SerialName("createdAt") val createdAt: Long,
    @SerialName("user") val user: User,
    @SerialName("likes") val likes: List<User>? = null
)
