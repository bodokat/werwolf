package roles

import GameData
import Player

object Freimaurer: Role {
   override val team = Team.Dorf
   override val group = Group.Mensch

    override fun behavior(): RoleBehavior = FreimaurerBehavior()

    override fun toString(): String = "Freimaurer"
}

class FreimaurerBehavior: RoleBehavior {
    override suspend fun beforeAsync(me: Player, data: GameData) {
        me.user.send("Du bist Freimaurer")
    }

    override suspend fun mainAsync(me: Player, data: GameData) {
        val others = data.players.filter { it.role == Freimaurer && it != me }.map { it.user.name }
        if (others.isEmpty()) me.user.send("Du bist alleine")
        else me.user.send("Die anderen Freimaurer sind: ${others.joinToString()}")
    }
}