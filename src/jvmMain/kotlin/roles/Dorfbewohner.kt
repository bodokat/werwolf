package roles

import GameData
import Player


object Dorfbewohner: Role {
    override val team = Team.Dorf
    override val group = Group.Mensch
    override fun behavior() = DorfbewohnerBehavior()
    override fun toString() = "Dorfbewohner"
}

class DorfbewohnerBehavior: RoleBehavior {
    override suspend fun beforeAsync(me: Player, data: GameData) {
        me.user.send("Du bist Dorfbewohner.")
    }
}