import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import lobby.Lobby
import lobby.ToClientMessage
import roles.Role
import roles.RoleBehavior
import roles.Team
import roles.Werwolf

suspend fun Lobby.startGame() = coroutineScope {
    assert(users.isNotEmpty())
    assert(roles.sum() > users.size)

    sendAll(ToClientMessage.Started)

    val gameData = setup()

    gameData.players.map { player -> async { player.roleBehavior.beforeAsync(player, gameData) } }.map { it.await() }
    gameData.players.forEach { player -> player.roleBehavior.beforeSync(player, gameData) }

    gameData.players.map { player -> async { player.roleBehavior.mainAsync(player, gameData) } }.map { it.await() }
    gameData.players.forEach { player -> player.roleBehavior.mainSync(player, gameData) }

    gameData.players.map { player -> async { player.roleBehavior.afterAsync(player, gameData) } }.map { it.await() }
    gameData.players.forEach { player -> player.roleBehavior.afterSync(player, gameData) }


    val playerVotes = gameData.players.map { player -> async { player.user.choice("Die Abstimmung hat begonnen! Welchen Spieler wählst du?",gameData.players) } }.map { it.await() }
    val totalVotes = playerVotes.fold(MutableList(playerVotes.size) { 0 }) { acc, vote -> acc.apply { vote.let { this[vote]++ } } }

    sendAll("Stimmen:\n")
    sendAll(gameData.players.mapIndexed { index, player -> "$player: ${totalVotes[index]}" }.joinToString("\n"))

    val maxVotes = totalVotes.maxOrNull()!!
    val deadPlayers = if (maxVotes <= 1) {
        emptyList()
    } else {
        gameData.players.filterIndexed { index, _ -> totalVotes[index] == maxVotes }
    }

    sendAll("Diese Spieler sind gestorben: ${deadPlayers.joinToString()}")

    val hasWerwolf = gameData.players.any { it.role == Werwolf }

    val winningTeam: Team = if (hasWerwolf) {
        if (deadPlayers.any { it.role == Werwolf }) Team.Dorf
        else Team.Wolf
    } else {
        if (deadPlayers.isEmpty()) Team.Dorf
        else Team.Wolf
    }

    sendAll(when (winningTeam) {
        Team.Dorf -> "Das Dorf hat gewonnen"
        Team.Wolf -> "Die Werwölfe haben gewonnen"
    })

    sendAll(ToClientMessage.Ended)

    running.set(false)
}

fun Lobby.setup(): GameData {
    val actualRoles = this.roles.flatMapIndexed { index: Int, amount: Int -> List(amount) {availableRoles[index]} }
    val shuffledRoles = actualRoles.shuffled().toMutableList()

    val players = this.users.map {
        val role = shuffledRoles.removeFirst()
        Player(it, role, role.behavior())
    }
    return GameData(players, shuffledRoles)
}

data class Player(val user: User, var role: Role, val roleBehavior: RoleBehavior) {
    override fun toString() = user.name
}

data class GameData(val players: List<Player>, val extra_roles: List<Role>)