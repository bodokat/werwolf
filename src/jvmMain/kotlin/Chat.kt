
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

fun Route.chatRoute() {
    val chatrooms: MutableMap<String?, Chatroom> = Collections.synchronizedMap(HashMap())
    val lastUserID = AtomicInteger(0)
    webSocket("/chat/{room}") {
        val name = call.parameters["name"] ?: "user${lastUserID.getAndIncrement()}"
        val thisConnection = Connection(this, name)
        val room = call.parameters["room"]
        val chatroom = chatrooms.computeIfAbsent(room) { Chatroom() }
        try {
            chatroom.add(thisConnection)
            chatroom.send("Welcome, ${thisConnection.name}! There are ${chatroom.connections.size} user(s) currently connected.")
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val received = frame.readText()
                val textWithName = "[${thisConnection.name}]: $received"
                chatroom.send(textWithName)
            }
        } catch (e: Exception) {
            println(e.message)
        } finally {
            println("removing $thisConnection!")
            chatroom.send("Goodbye, ${thisConnection.name}!")
            chatroom.remove(thisConnection)
            if (chatroom.connections.isEmpty()) {
                chatrooms.remove(name)
            }
        }
    }
}

class Chatroom {
    val connections: MutableSet<Connection> = Collections.synchronizedSet(LinkedHashSet())

    fun add(connection: Connection) {
        connections.add(connection)
    }

    fun remove(connection: Connection) {
        connections.remove(connection)
    }

    suspend fun send(message: String) = coroutineScope {
        connections.map { async {it.session.send(message)} }.map { it.await() }
    }
}

data class Connection(val session: DefaultWebSocketSession, val name: String)