package streamix.server.auth

import java.sql.Connection
import java.sql.DriverManager
import javax.sql.DataSource

class PostgresUserRepository(
    private val dataSource: DataSource
) : UserRepository {
    init {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS anilab_users (
                        uid TEXT PRIMARY KEY,
                        email TEXT,
                        nickname TEXT NOT NULL,
                        avatar_url TEXT,
                        bio TEXT,
                        created_at BIGINT NOT NULL,
                        updated_at BIGINT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }

    override suspend fun find(uid: String): AniLabUser? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "SELECT uid,email,nickname,avatar_url,bio,created_at,updated_at FROM anilab_users WHERE uid=?"
            ).use { statement ->
                statement.setString(1, uid)
                statement.executeQuery().use { rs ->
                    if (!rs.next()) return@use null
                    AniLabUser(
                        uid = rs.getString("uid"),
                        email = rs.getString("email"),
                        nickname = rs.getString("nickname"),
                        avatarUrl = rs.getString("avatar_url"),
                        bio = rs.getString("bio"),
                        createdAtEpochMillis = rs.getLong("created_at"),
                        updatedAtEpochMillis = rs.getLong("updated_at")
                    )
                }
            }
        }

    override suspend fun createOrUpdateIdentity(
        uid: String,
        email: String?,
        defaultNickname: String
    ): AniLabUser {
        val now = System.currentTimeMillis()
        val existing = find(uid)
        if (existing != null) {
            dataSource.connection.use { c ->
                c.prepareStatement(
                    "UPDATE anilab_users SET email=?, updated_at=? WHERE uid=?"
                ).use { s ->
                    s.setString(1, email ?: existing.email)
                    s.setLong(2, now)
                    s.setString(3, uid)
                    s.executeUpdate()
                }
            }
            return existing.copy(
                email = email ?: existing.email,
                updatedAtEpochMillis = now
            )
        }

        val user = AniLabUser(
            uid = uid,
            email = email,
            nickname = defaultNickname.ifBlank { email?.substringBefore("@") ?: "User" },
            avatarUrl = null,
            bio = null,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )

        dataSource.connection.use { c ->
            c.prepareStatement(
                "INSERT INTO anilab_users(uid,email,nickname,avatar_url,bio,created_at,updated_at) VALUES(?,?,?,?,?,?,?)"
            ).use { s ->
                s.setString(1, user.uid)
                s.setString(2, user.email)
                s.setString(3, user.nickname)
                s.setString(4, user.avatarUrl)
                s.setString(5, user.bio)
                s.setLong(6, user.createdAtEpochMillis)
                s.setLong(7, user.updatedAtEpochMillis)
                s.executeUpdate()
            }
        }
        return user
    }

    override suspend fun updateProfile(
        uid: String,
        nickname: String,
        avatarUrl: String?,
        bio: String?
    ): AniLabUser {
        require(nickname.isNotBlank())
        val now = System.currentTimeMillis()

        dataSource.connection.use { c ->
            c.prepareStatement(
                "UPDATE anilab_users SET nickname=?,avatar_url=?,bio=?,updated_at=? WHERE uid=?"
            ).use { s ->
                s.setString(1, nickname.trim())
                s.setString(2, avatarUrl)
                s.setString(3, bio)
                s.setLong(4, now)
                s.setString(5, uid)
                if (s.executeUpdate() == 0) {
                    error("User does not exist: $uid")
                }
            }
        }
        return find(uid) ?: error("User disappeared after update: $uid")
    }
}

fun postgresDataSource(): DataSource {
    val url = System.getenv("ANILAB_DATABASE_URL")
        ?: error("ANILAB_DATABASE_URL is required")
    val user = System.getenv("ANILAB_DATABASE_USER")
        ?: error("ANILAB_DATABASE_USER is required")
    val password = System.getenv("ANILAB_DATABASE_PASSWORD")
        ?: error("ANILAB_DATABASE_PASSWORD is required")

    return object : DataSource {
        override fun getConnection(): Connection =
            DriverManager.getConnection(url, user, password)
        override fun getConnection(username: String?, password: String?): Connection =
            DriverManager.getConnection(url, username, password)
        override fun unwrap(iface: Class<*>?): Any = throw java.sql.SQLException("Unsupported")
        override fun isWrapperFor(iface: Class<*>?): Boolean = false
        override fun setLogWriter(out: java.io.PrintWriter?) {}
        override fun getLogWriter(): java.io.PrintWriter? = null
        override fun setLoginTimeout(seconds: Int) {}
        override fun getLoginTimeout(): Int = 0
        override fun getParentLogger(): java.util.logging.Logger =
            java.util.logging.Logger.getLogger("AniLab")
    }
}
