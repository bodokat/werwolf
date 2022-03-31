package login

import api.newLobby
import csstype.*
import game.scope
import kotlinx.coroutines.launch
import mui.material.Box
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.TextField
import mui.system.sx
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.ReactNode
import react.dom.onChange
import react.router.NavigateOptions
import react.router.useNavigate
import react.useState

val Login = FC<Props> {
    val (name, setName) = useState("")
    val navigate = useNavigate()
    Box {
        sx {
            display = Display.grid
            gridTemplateRows = array(1.fr, 1.fr)
            alignItems = AlignItems.center
            paddingTop = 7.rem
        }
        Box {
            sx {
                marginLeft = Auto.auto
                marginRight = Auto.auto
                display = Display.flex
            }
            TextField {
                label = ReactNode("Enter Name")
                value = name
                onChange = {
                    setName((it.target as HTMLInputElement).value)
                }
            }
            Button {
                +"New Game"
                variant = ButtonVariant.contained
                onClick = {
                    scope.launch {
                        val lobby = newLobby()
                        navigate("/l/$lobby", options = object : NavigateOptions {
                            override var replace: Boolean? = null
                            override var state: Any? = name
                        })
                    }
                }
            }
        }
    }
}