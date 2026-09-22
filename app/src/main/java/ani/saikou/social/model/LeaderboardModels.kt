package ani.saikou.social.model

import kotlinx.serialization.Serializable

@Serializable
enum class LeaderboardType {
    WATCH_TIME,
    EPISODES,
    COMPLETED,
    SOCIAL,
    WATCH_TOGETHER
}

@Serializable
enum class LeaderboardPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    ALL_TIME
}

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val user: SocialUser,
    val value: Long
)

@Serializable
data class Leaderboard(
    val type: LeaderboardType,
    val period: LeaderboardPeriod,
    val entries: List<LeaderboardEntry> = emptyList()
)
