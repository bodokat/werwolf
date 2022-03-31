package roles

import GameData
import Player

object Unruhestifterin: Role {
    override val team = Team.Dorf
    override val group = Group.Mensch
    override fun behavior() = UnruhestifterinBehavior()
    override fun toString() = "Unruhestifterin"
}

class UnruhestifterinBehavior: RoleBehavior {
    override suspend fun beforeAsync(me: Player, data: GameData) {
        me.user.send("Du bist Unruhestifterin.")
    }

    private var toSwap1: Player? = null
    private var toSwap2: Player? = null
    override suspend fun mainAsync(me: Player, data: GameData) {
        val others = data.players.filter { it != me }
        toSwap1 = me.user.choice("Wen willst du tauschen?", others).let { others[it] }
        val others2 = others.filter { it != toSwap1 }
        toSwap2 = me.user.choice("Mit wem wllst du $toSwap1 tauschen?", others2).let { others2[it] }
    }

    override fun mainSync(me: Player, data: GameData) {
        toSwap1?.let { swap1 -> toSwap2?.let { swap2 ->
            swap1.role = swap2.role.also { swap2.role = swap1.role }
        } }
    }
}
