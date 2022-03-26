package roles

import GameData
import Player


object Werwolf: Role {
    override val team = Team.Wolf
    override val group = Group.Wolf
    override fun behavior() = WerwolfBehavior()
    override fun toString() = "Werwolf"
}

class WerwolfBehavior: RoleBehavior {
    override suspend fun beforeAsync(me: Player, data: GameData) {
        me.user.send("Du bist Werwolf.")
    }

    override suspend fun mainAsync(me: Player, data: GameData) {
        val others = data.players.filter { it.role == Werwolf && it != me }.map { it.user.name }
        if (others.isEmpty()) {
            val randRole = data.extra_roles.random()
            me.user.send("Du bist alleine. Eine Rolle in der Mitte ist: $randRole")
        } else {
            me.user.send("Die anderen Werwölfe sind: ${others.joinToString()}")
        }
    }
}