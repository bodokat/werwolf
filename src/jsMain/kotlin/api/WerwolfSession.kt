package api

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.cio.websocket.*
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lobby.LobbySettings
import lobby.ToClientMessage
import lobby.ToServerMessage

val origin = window.location

class WerwolfSession private constructor(
    private val socket: DefaultClientWebSocketSession,
    val lobby: String,
    val messages: Flow<ToClientMessage>,
    initialState: SessionState
) {
    companion object {
        suspend fun join(lobby: String, name: String): WerwolfSession = coroutineScope {
            val client = HttpClient {
                install(WebSockets)
            }
            val socket = client.webSocketSession {
                this.url(
                    // use non-ssl (ws) when connecting to localhost
                    scheme = if (origin.hostname.let { it == "0.0.0.0" || it == "localhost" }) "ws" else "wss",
                    host = origin.host,
                    path = "/api/werwolf/$lobby?name=$name"
                )
                println("Connecting to: ${url.buildString()}")
            }
            val messages = socket.incoming.receiveAsFlow() //TODO
                .mapNotNull { it as? Frame.Text }
                .mapNotNull {
                    try {
                        Json.decodeFromString<ToClientMessage>(it.readText())
                    } catch (e: SerializationException) {
                        println("Error while deserializing message: $e")
                        null
                    }
                }
            val initialState = messages.filterIsInstance<ToClientMessage.Initial>().first().toState()
            WerwolfSession(socket, lobby, messages, initialState)
        }
    }

    private val scope =  CoroutineScope(Dispatchers.Default)

    val state: StateFlow<SessionState> = messages.scan(initialState) { state: SessionState, message: ToClientMessage ->
        val newState = when (message) {
            is ToClientMessage.Initial -> {
                println("Warning: Received additional Initial message")
                state
            }
            is ToClientMessage.Joined -> {
                if(!state.players.contains(message.player)) state.copy(players = state.players + message.player)
                else state
            }

            is ToClientMessage.Left ->
                state.copy(players = state.players - message.player)

            ToClientMessage.Started -> state.copy(started = true)
            is ToClientMessage.Text -> state
            is ToClientMessage.Question -> state
            ToClientMessage.Ended -> state.copy(started = false)
            is ToClientMessage.NewSettings -> state.copy(settings = message.settings)
        }
        newState.copy(messages = state.messages + message)
    }.stateIn(scope,SharingStarted.Eagerly,initialState)

    suspend fun disconnect() {
        socket.close()
        scope.cancel()
    }


    suspend fun send(message: ToServerMessage) {
        this.socket.send(Json.encodeToString(message))
    }
}

suspend fun newLobby(): String {
    val client = HttpClient()
    val id = client.post<String> {
        this.url(
            // use non-ssl (http) when connecting to localhost
            scheme = if (origin.hostname.let { it == "0.0.0.0" || it == "localhost" }) "http" else "https",
            host = origin.host,
            path = "/api/new"
        )
    }
    println("Created lobby with id $id")
    return id
}

data class SessionState(
    val messages: List<ToClientMessage>,
    val players: List<String>,
    val me: String,
    val started: Boolean,
    val settings: LobbySettings
)

fun ToClientMessage.Initial.toState(): SessionState = SessionState(
    messages = emptyList(),
    players = this.players,
    me = me,
    started = false,
    settings = this.settings
)