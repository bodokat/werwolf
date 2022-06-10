package lobby

import User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import roles.*
import startGame
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class Lobby {
    var users: List<User> = Collections.synchronizedList(mutableListOf())
    val availableRoles: List<Role> =
        listOf(Dorfbewohner, Werwolf, Dieb, Doppel, Freimaurer, Schlaflose, Seherin, Unruhestifterin)
    var roles: List<Int> = Collections.synchronizedList(listOf(1, 2, 1, 0, 2, 1, 1, 1))


    var running = AtomicBoolean(false)
    var nameCounter = AtomicInteger(1)

    private fun canStart(): Boolean =
        users.size < roles.size


    suspend fun sendAll(message: ToClientMessage) {
        users.forEach { it.send(message) }
    }

    suspend fun sendAll(text: String) {
        sendAll(ToClientMessage.Text(text))
    }

    suspend fun handleMessage(message: ToServerMessage, user: User) {
        if (message is ToServerMessage.AdminMessage) {
            if (user != users.first()) {
                return
            } else {
                when (message) {
                    is ToServerMessage.AdminMessage.KickPlayer -> this.users = users.drop(message.index)
                    is ToServerMessage.AdminMessage.Start -> if (canStart()) startGame()
                    is ToServerMessage.AdminMessage.ChangeRoles -> {
                        roles = message.newRoles
                        sendAll(ToClientMessage.NewSettings(
                            LobbySettings(
                                admin = users.first().name,
                                roles = roles.map { if (it >= 0) it else 0 },
                                availableRoles = availableRoles.map { it.toString() }
                            )
                        ))
                    }
                }
            }
        } else when (message) {
            is ToServerMessage.Response -> user.response(message.id, message.choice)
            else -> {}
        }
    }
}

fun Route.werwolfRoute() {
    val lobbies: MutableMap<String?, Lobby> = Collections.synchronizedMap(HashMap())

    webSocket("/werwolf/{lobby}") {
        val lobbyName = call.parameters["lobby"]
        val lobby = lobbies.computeIfAbsent(lobbyName) { Lobby() }
        val userName = call.request.queryParameters["name"] ?: return@webSocket call.respondText(
            "no name provided",
            status = HttpStatusCode.BadRequest
        )

        val name = lobby.validate(userName)
        val user = User(name, this)

        try {
            addUser(lobby, user)
        } catch (e: Exception) {
            println("Exception: $e (${e.message})")
        } finally {
            println("removing $user!")
            lobby.users -= user
            if (lobby.users.isEmpty()) {
                println("Removing lobby")
                lobbies.remove(lobbyName)
            }
        }
    }


    post("new") {
        val charPool: List<Char> = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        var id = List(8) {
            charPool.random()
        }.joinToString("")
        while (lobbies.containsKey(id)) {
            id = List(8) {
                charPool.random()
            }.joinToString("")
        }
        lobbies[id] = Lobby()
        call.respond(id)
    }
}

private suspend fun DefaultWebSocketServerSession.addUser(
    lobby: Lobby,
    user: User,
) {
    lobby.users = lobby.users + user
    launch {
        lobby.sendAll(ToClientMessage.Joined(user.name))
    }
    user.send(
        ToClientMessage.Initial(
            players = lobby.users.map { it.toString() },
            settings = LobbySettings(
                availableRoles = lobby.availableRoles.map(Role::toString),
                roles = lobby.roles,
                admin = lobby.users[0].name,
            ),
            me = user.name
        )
    )
    incoming.receiveAsFlow()
        .mapNotNull { it as? Frame.Text }
        .mapNotNull {
            try {
                Json.decodeFromString<ToServerMessage>(it.readText())
            } catch (e: SerializationException) {
                println("Error while decoding message: $e")
                null
            }
        }
        .map { launch { lobby.handleMessage(it, user) } }
        .collect()
}

fun Lobby.validate(name: String): String {
    return if (name.matches(Regex("[A-Za-z\\d\\s]+")) && name.length <= 20 && users.none { it.name == name }) name
    else validate("Uhrensohn ${nameCounter.getAndIncrement()}")
}
