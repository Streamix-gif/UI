package streamix.social
import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
class SocialHub{
    private val rooms=ConcurrentHashMap<String,MutableSet<DefaultWebSocketServerSession>>()
    private val locks=ConcurrentHashMap<String,Mutex>()
    suspend fun join(room:String,s:DefaultWebSocketServerSession){locks.computeIfAbsent(room){Mutex()}.withLock{rooms.computeIfAbsent(room){linkedSetOf()}.add(s)}}
    suspend fun leave(room:String,s:DefaultWebSocketServerSession){locks[room]?.withLock{rooms[room]?.remove(s)}}
    suspend fun broadcast(room:String,message:String){val ss=locks[room]?.withLock{rooms[room]?.toList().orEmpty()}?:return;ss.forEach{runCatching{it.send(Frame.Text(message))}}}
}
