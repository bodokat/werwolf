package roles

import GameData
import Player

object Seherin: Role {
    override val team = Team.Dorf
    override val group = Group.Mensch

    override fun behavior() = SeherinBehavior()

    override fun toString(): String = "Seherin"
}

class SeherinBehavior: RoleBehavior {
    override suspend fun beforeAsync(me: Player, data: GameData) {
        me.user.send("Du bist Seherin")
    }

    override suspend fun mainAsync(me: Player, data: GameData) {
        val others = data.players.filter { it != me }
        val choices = others.plus("Eine Rolle aus der Mitte")
        val response = me.user.choice("Wessen Rolle willst du sehen", choices)
        if (response < others.size) {
            val player = others[response]
            me.user.send("Die Rolle von $player ist ${player.role}")
        } else {
            val role = data.extra_roles.random()
            me.user.send("Eine Rolle in der Mitte ist $role")
        }
    }
}