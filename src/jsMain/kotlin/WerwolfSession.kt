import chat.endpoint
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.cio.websocket.*
import kotlinx.browser.window
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lobby.ToClientMessage
import lobby.ToServerMessage

val origin = window.location

class WerwolfSession private constructor(private val socket: DefaultClientWebSocketSession, val lobby: String) {
    companion object {
        suspend fun connect(lobby: String): WerwolfSession {
            val client = HttpClient {
                install(WebSockets)
            }
            val socket = client.webSocketSession {
                this.url(
                    scheme = "ws",
                    host = origin.host,
                    port = origin.port.toInt(),
                    path = "/api/werwolf/$lobby"
                )
            }
            return WerwolfSession(socket, lobby)
        }
    }

    val messages = socket.incoming.consumeAsFlow()
        .mapNotNull { it as? Frame.Text }
        .mapNotNull {
            try {
                Json.decodeFromString<ToClientMessage>(it.readText())
            } catch (e: SerializationException) {
                println("Error while deserializing message: $e")
                null
            }
        }

    suspend fun send(message: ToServerMessage) {
        this.socket.send(Json.encodeToString(message))
    }
}