package roles

import GameData
import Player

object Schlaflose: Role {
    override val team = Team.Dorf
    override val group = Group.Mensch
    override fun behavior() = SchlafloseBehavior()
    override fun toString() = "Schlaflose"
}

class SchlafloseBehavior: RoleBehavior {
    override suspend fun beforeAsync(me: Player, data: GameData) {
        me.user.send("Du bist Schlaflose.")
    }

    override suspend fun afterAsync(me: Player, data: GameData) {
        me.user.send("Deine Rolle ist jetzt ${me.role}")
    }
}
