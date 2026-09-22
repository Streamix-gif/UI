package ani.saikou.social.repository

import ani.saikou.backend.BackendResult
import ani.saikou.social.model.*

/**
 * Local/demo implementation used while the Streamix Social backend is not connected.
 * It keeps Social UI independent from AniList; anime IDs are metadata references only.
 */
object LocalSocialRepository : SocialRepository {
    private val currentUser = SocialUser(
        id = "local-user",
        username = "streamix_user",
        displayName = "User",
        bio = "Your anime profile",
        animeWatched = 128,
        episodesWatched = 1462,
        favoriteMediaIds = listOf(21, 1535, 5114)
    )

    private val users = listOf(
        currentUser,
        SocialUser("kael", "kael", "Kael"),
        SocialUser("rynn", "rynn", "Rynn"),
        SocialUser("hana", "hana", "Hana")
    )

    override suspend fun getCurrentUser() = BackendResult.Success(currentUser)
    override suspend fun getUser(userId: String) =
        BackendResult.Success(users.firstOrNull { it.id == userId } ?: currentUser)

    override suspend fun searchUsers(query: String) =
        BackendResult.Success(users.filter { it.displayName.contains(query, true) })

    override suspend fun getRelationships(userId: String) =
        BackendResult.Success(emptyList<Relationship>())

    override suspend fun getActivities(userId: String?, page: Int) =
        BackendResult.Success(
            listOf(
                SocialActivity("activity-1", currentUser, "WATCHING", "is watching One Piece", "21", System.currentTimeMillis() / 1000 - 600),
                SocialActivity("activity-2", users[1], "COMPLETED", "just finished Solo Leveling", "1535", System.currentTimeMillis() / 1000 - 7200),
                SocialActivity("activity-3", users[2], "ROOM", "created a Watch Together room", null, System.currentTimeMillis() / 1000 - 10800)
            ).filter {
                when {
                    userId == null -> true
                    userId == currentUser.id -> it.author.id == currentUser.id
                    else -> true
                }
            }
        )

    override suspend fun getNotifications(userId: String, page: Int) =
        BackendResult.Success(emptyList<SocialNotification>())

    override suspend fun getConversations(userId: String) =
        BackendResult.Success(emptyList<Conversation>())

    override suspend fun getMessages(conversationId: String, page: Int) =
        BackendResult.Success(emptyList<Message>())

    override suspend fun getChatRooms(mediaId: String?, episodeId: String?) =
        BackendResult.Success(emptyList<ChatRoom>())

    override suspend fun getChatMessages(roomId: String, page: Int) =
        BackendResult.Success(emptyList<ChatMessage>())

    override suspend fun getLeaderboard(type: LeaderboardType, period: LeaderboardPeriod) =
        BackendResult.Success(
            Leaderboard(
                type = type,
                period = period,
                entries = users.mapIndexed { index, user ->
                    LeaderboardEntry(
                        rank = index + 1,
                        user = user,
                        value = listOf(15230L, 12450L, 10980L, 8420L)[index]
                    )
                }
            )
        )

    override suspend fun getWatchRoom(roomId: String) =
        BackendResult.HttpError(404, "Room not found")

    override suspend fun getRoomMembers(roomId: String) =
        BackendResult.Success(emptyList<RoomMember>())

    override suspend fun getPlaybackState(roomId: String) =
        BackendResult.HttpError(404, "Room not found")
}
