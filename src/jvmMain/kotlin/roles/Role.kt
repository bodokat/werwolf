package roles

import GameData
import Player


interface Role {
    val team: Team
    val group: Group

    fun behavior(): RoleBehavior
}

interface RoleBehavior {
    suspend fun beforeAsync(me: Player, data: GameData) {}
    fun beforeSync(me: Player, data: GameData) {}
    suspend fun mainAsync(me: Player, data: GameData) {}
    fun mainSync(me: Player, data: GameData) {}
    suspend fun afterAsync(me: Player, data: GameData) {}
    fun afterSync(me: Player, data: GameData) {}
}

enum class Team {
    Dorf,
    Wolf
}

enum class Group {
    Mensch,
    Wolf
}