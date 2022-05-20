package game

import csstype.Display
import csstype.px
import mui.material.Box
import mui.material.Card
import mui.material.Chip
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.useContext

val Players = FC<Props> {
    val state = useContext(gameContext)

    Box {
        sx {
            display = Display.flex
        }
        state.players.forEach {
            Card {
                sx {
                    margin = 10.px
                }
                Typography {
                    variant = TypographyVariant.h6
                    +it
                }
                if(it == state.me) Chip {
                    label = ReactNode("me")
                }
                if(it == state.settings.admin) Chip {
                    label = ReactNode("admin")
                }
            }
        }
    }
}