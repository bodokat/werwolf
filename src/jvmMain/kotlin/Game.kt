import kotlinx.coroutines.CoroutineScope
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

    // Action Phase

    gameData.players.map { player -> async { player.roleBehavior.beforeAsync(player, gameData) } }.map { it.await() }
    gameData.players.forEach { player -> player.roleBehavior.beforeSync(player, gameData) }

    gameData.players.map { player -> async { player.roleBehavior.mainAsync(player, gameData) } }.map { it.await() }
    gameData.players.forEach { player -> player.roleBehavior.mainSync(player, gameData) }

    gameData.players.map { player -> async { player.roleBehavior.afterAsync(player, gameData) } }.map { it.await() }
    gameData.players.forEach { player -> player.roleBehavior.afterSync(player, gameData) }

    // Voting Phase
    val (deadPlayers, playerVotes) = vote(gameData)

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

    println("Game ended")
    running.set(false)
}

context(CoroutineScope)
private suspend fun Lobby.vote(
    gameData: GameData,
): Pair<List<Player>, List<Int>> {
    val skip = object {
        override fun toString() = "skip"
    }
    val voteChoices = gameData.players + skip
    val playerVotes = gameData.players.map { (user, _, _) ->
        async {
            user.choice(
                "Die Abstimmung hat begonnen! Welchen Spieler wählst du?",
                voteChoices
            )
        }
    }.map { it.await() }
    val totalVotes = playerVotes.fold(MutableList(voteChoices.size) { 0 }) { acc, vote -> acc.apply { this[vote]++ } }

    sendAll("Stimmen:\n")
    sendAll(voteChoices.mapIndexed { index, player -> "$player: ${voteChoices[index]}" }.joinToString("\n"))

    val maxVotes = totalVotes.maxOrNull()!!
    return Pair(gameData.players.filterIndexed { index, _ -> totalVotes[index] == maxVotes },playerVotes)
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