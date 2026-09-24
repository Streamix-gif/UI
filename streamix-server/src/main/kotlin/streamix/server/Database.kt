package streamix.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection

class Database(jdbcUrl: String, username: String, password: String) {
    private val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = jdbcUrl
        this.username = username
        this.password = password
        maximumPoolSize = System.getenv("DB_POOL_SIZE")?.toIntOrNull() ?: 10
        minimumIdle = 1
    })

    suspend fun <T> query(block: (Connection) -> T): T = withContext(Dispatchers.IO) {
        dataSource.connection.use(block)
    }

    fun migrate() {
        dataSource.connection.use { c ->
            c.createStatement().use { statement ->
                SCHEMA.split(";").map(String::trim).filter(String::isNotEmpty).forEach(statement::execute)
            }
        }
    }

    fun close() = dataSource.close()

    companion object {
        private val SCHEMA = """
            CREATE TABLE IF NOT EXISTS users (uid TEXT PRIMARY KEY,email TEXT,nickname TEXT NOT NULL,avatar_url TEXT,bio TEXT,last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW());
            CREATE TABLE IF NOT EXISTS friendships (user_uid TEXT NOT NULL REFERENCES users(uid) ON DELETE CASCADE,friend_uid TEXT NOT NULL REFERENCES users(uid) ON DELETE CASCADE,status TEXT NOT NULL CHECK(status IN ('pending','accepted','blocked')),created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),PRIMARY KEY(user_uid,friend_uid));
            CREATE TABLE IF NOT EXISTS chat_messages (id UUID PRIMARY KEY,room_type TEXT NOT NULL CHECK(room_type IN ('global','anime','watch')),room_id TEXT,sender_uid TEXT NOT NULL REFERENCES users(uid) ON DELETE CASCADE,body TEXT NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT NOW());
            CREATE INDEX IF NOT EXISTS chat_messages_room_idx ON chat_messages(room_type,room_id,created_at DESC);
            CREATE TABLE IF NOT EXISTS social_points (uid TEXT PRIMARY KEY REFERENCES users(uid) ON DELETE CASCADE,points BIGINT NOT NULL DEFAULT 0,updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW());
            CREATE TABLE IF NOT EXISTS friend_activity (id UUID PRIMARY KEY,actor_uid TEXT NOT NULL REFERENCES users(uid) ON DELETE CASCADE,activity_type TEXT NOT NULL,payload JSONB NOT NULL DEFAULT '{}'::jsonb,created_at TIMESTAMPTZ NOT NULL DEFAULT NOW());
            CREATE INDEX IF NOT EXISTS friend_activity_actor_idx ON friend_activity(actor_uid,created_at DESC);
            CREATE TABLE IF NOT EXISTS watch_rooms (id UUID PRIMARY KEY,owner_uid TEXT NOT NULL REFERENCES users(uid) ON DELETE CASCADE,anime_id TEXT NOT NULL,episode_number INT NOT NULL,state TEXT NOT NULL DEFAULT 'paused' CHECK(state IN ('playing','paused','ended')),position_ms BIGINT NOT NULL DEFAULT 0,created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW());
            CREATE TABLE IF NOT EXISTS watch_room_members (room_id UUID NOT NULL REFERENCES watch_rooms(id) ON DELETE CASCADE,uid TEXT NOT NULL REFERENCES users(uid) ON DELETE CASCADE,joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),PRIMARY KEY(room_id,uid));
        """.trimIndent()
    }
}
