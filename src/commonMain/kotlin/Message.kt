package lobby

import kotlinx.serialization.Serializable

@Serializable
class LobbySettings(
    val availableRoles: List<String>,
    val roles: List<Int>,
    val admin: String,
)

@Serializable
sealed class ToServerMessage {
    /**
     * Response to a [ToClientMessage.Question]
     */
    @Serializable
    class Response(val id: Int, val choice: Int) : ToServerMessage()

    sealed class AdminMessage : ToServerMessage() {
        @Serializable
        object Start : AdminMessage()

        @Serializable
        class KickPlayer(val index: Int) : AdminMessage()

        @Serializable
        class changeRoles(val newRoles: List<Int>): AdminMessage()
    }
}

@Suppress("unused")
@Serializable
sealed class ToClientMessage {

    @Serializable
    class Initial(val settings: LobbySettings, val players: List<String>): ToClientMessage()
    @Serializable
    class Joined(val player: String): ToClientMessage()

    @Serializable
    class Left(val player: String): ToClientMessage()

    @Serializable
    object Started: ToClientMessage()

    /**
     * Message sent while inside a game
     */
    sealed class IngameMessage: ToClientMessage()

    @Serializable
    class Text(val text: String): IngameMessage()

    @Serializable
    class Question(val id: Int, val text: String, val options: List<String>): IngameMessage()

    @Serializable
    object Ended : ToClientMessage()


}

