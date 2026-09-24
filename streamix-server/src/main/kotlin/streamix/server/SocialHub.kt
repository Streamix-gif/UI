package streamix.server

import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class SocialHub {
    private val rooms = ConcurrentHashMap<String, MutableSet<DefaultWebSocketServerSession>>()
    private val locks = ConcurrentHashMap<String, Mutex>()

    suspend fun join(room: String, session: DefaultWebSocketServerSession) {
        val lock = locks.computeIfAbsent(room) { Mutex() }
        lock.withLock { rooms.computeIfAbsent(room) { linkedSetOf() }.add(session) }
    }

    suspend fun leave(room: String, session: DefaultWebSocketServerSession) {
        locks[room]?.withLock { rooms[room]?.remove(session) }
    }

    suspend fun broadcast(room: String, message: String) {
        val sessions = locks[room]?.withLock { rooms[room]?.toList().orEmpty() } ?: return
        sessions.forEach { runCatching { it.send(Frame.Text(message)) } }
    }
}
