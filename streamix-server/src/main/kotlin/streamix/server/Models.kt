package streamix.server

import kotlinx.serialization.Serializable

@Serializable data class GoogleAuthRequest(val idToken: String)
@Serializable data class AuthResponse(val token: String, val user: UserDto)
@Serializable data class UserDto(val uid: String, val email: String?, val nickname: String, val avatarUrl: String?, val bio: String?)
@Serializable data class ProfileUpdateRequest(val nickname: String, val avatarUrl: String? = null, val bio: String? = null)
@Serializable data class FriendDto(val uid: String, val nickname: String, val avatarUrl: String?, val status: String)
@Serializable data class ChatMessageDto(val id: String, val roomType: String, val roomId: String?, val sender: UserDto, val body: String, val createdAt: String)
@Serializable data class SendMessageRequest(val roomType: String, val roomId: String? = null, val body: String)
@Serializable data class LeaderboardEntryDto(val uid: String, val nickname: String, val avatarUrl: String?, val points: Long)
@Serializable data class ActivityDto(val id: String, val actor: UserDto, val type: String, val payload: Map<String, String>, val createdAt: String)
@Serializable data class WatchRoomDto(val id: String, val ownerUid: String, val animeId: String, val episodeNumber: Int, val state: String, val positionMs: Long)
@Serializable data class CreateWatchRoomRequest(val animeId: String, val episodeNumber: Int, val positionMs: Long = 0)
@Serializable data class UpdateWatchRoomRequest(val state: String, val positionMs: Long)
@Serializable data class SocialDashboardDto(val friends: List<FriendDto>, val leaderboard: List<LeaderboardEntryDto>, val recentMessages: List<ChatMessageDto>, val friendActivity: List<ActivityDto>, val activeFriends: List<FriendDto>)
@Serializable data class SocketMessage(val body: String)
@Serializable data class ErrorDto(val error: String)
