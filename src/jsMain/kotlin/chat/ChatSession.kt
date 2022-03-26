package chat

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.cio.websocket.*
import kotlinx.browser.window
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

val endpoint = window.location.origin

class ChatSession private constructor(private val socket: DefaultClientWebSocketSession, val room: String) {
    companion object {
        suspend fun connect(room: String): ChatSession {
            val client = HttpClient {
                install(WebSockets)
            }
            val socket = client.webSocketSession {
                this.url("$endpoint/api/chat/$room".replace("http", "ws")) //TODO: Make this better
            }
            return ChatSession(socket, room)
        }
    }

    val messages = socket.incoming.consumeAsFlow()
        .mapNotNull { it as? Frame.Text }
        .map { it.readText() }

    suspend fun send(text: String) {
        this.socket.send(text)
    }
}