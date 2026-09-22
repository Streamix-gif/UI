package ani.saikou.social.repository

import ani.saikou.backend.BackendResult
import ani.saikou.social.model.ChatMessage
import ani.saikou.social.model.ChatRoom
import ani.saikou.social.model.Conversation
import ani.saikou.social.model.Leaderboard
import ani.saikou.social.model.LeaderboardPeriod
import ani.saikou.social.model.LeaderboardType
import ani.saikou.social.model.Message
import ani.saikou.social.model.PlaybackState
import ani.saikou.social.model.Relationship
import ani.saikou.social.model.RoomMember
import ani.saikou.social.model.SocialActivity
import ani.saikou.social.model.SocialNotification
import ani.saikou.social.model.SocialUser
import ani.saikou.social.model.WatchRoom

/**
 * Backend-agnostic Social contract.
 *
 * Implementations may use REST, GraphQL, WebSocket-backed services, or local
 * fakes/tests. UI code should depend on this interface rather than a provider.
 */
interface SocialRepository {
    suspend fun getCurrentUser(): BackendResult<SocialUser>
    suspend fun getUser(userId: String): BackendResult<SocialUser>
    suspend fun searchUsers(query: String): BackendResult<List<SocialUser>>
    suspend fun getRelationships(userId: String): BackendResult<List<Relationship>>
    suspend fun getActivities(userId: String? = null, page: Int = 1): BackendResult<List<SocialActivity>>
    suspend fun getNotifications(userId: String, page: Int = 1): BackendResult<List<SocialNotification>>

    suspend fun getConversations(userId: String): BackendResult<List<Conversation>>
    suspend fun getMessages(conversationId: String, page: Int = 1): BackendResult<List<Message>>

    suspend fun getChatRooms(mediaId: String? = null, episodeId: String? = null): BackendResult<List<ChatRoom>>
    suspend fun getChatMessages(roomId: String, page: Int = 1): BackendResult<List<ChatMessage>>

    suspend fun getLeaderboard(
        type: LeaderboardType,
        period: LeaderboardPeriod
    ): BackendResult<Leaderboard>

    suspend fun getWatchRoom(roomId: String): BackendResult<WatchRoom>
    suspend fun getRoomMembers(roomId: String): BackendResult<List<RoomMember>>
    suspend fun getPlaybackState(roomId: String): BackendResult<PlaybackState>
}
