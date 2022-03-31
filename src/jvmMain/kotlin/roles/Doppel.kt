package roles

import GameData
import Player

object Doppel: Role {
    override val team = Team.Dorf
    override val group = Group.Mensch
    override fun behavior() = DoppelBehavior()
    override fun toString() = "Doppelgängerin"
}

class DoppelBehavior: RoleBehavior {
    override suspend fun beforeAsync(me: Player, data: GameData) {
        me.user.send("Du bist Doppelgängerin.")
        val others = data.players.filter { it != me }
        toCopy = me.user.choice("Wen willst du kopieren?", others).let { others[it] }
    }

    override fun beforeSync(me: Player, data: GameData) {
        toCopy?.let { me.role = it.role }
    }

    private var toCopy: Player? = null
}
