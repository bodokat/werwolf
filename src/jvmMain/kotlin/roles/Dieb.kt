package roles

import GameData
import Player
import User

object Dieb: Role {
    override val team = Team.Dorf
    override val group = Group.Mensch
    override fun behavior() = DiebBehavior()
    override fun toString() = "Dieb"
}

class DiebBehavior: RoleBehavior {
    override suspend fun beforeAsync(me: Player, data: GameData) {
        me.user.send("Du bist Dieb.")
    }

    private var toSwap: Player? = null
    override suspend fun mainAsync(me: Player, data: GameData) {
        val others = data.players.filter { it != me }
        toSwap = me.user.choice("Mit wem willst du tauschen?", others).let { others[it] }
    }

    override fun mainSync(me: Player, data: GameData) {
        toSwap?.let { swap ->
            swap.role = me.role.also { me.role = swap.role }
        }
    }
}
