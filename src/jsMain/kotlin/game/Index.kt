package game

import api.WerwolfSession
import csstype.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mui.material.Box
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.TextField
import mui.system.sx
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.onChange
import react.router.*


val Game = FC<Props> {
    val navigate = useNavigate()
    val location = useLocation()
    val params = useParams()
    val lobby = params["lobby"] ?: run {
        return@FC Navigate {
            to="/"
        }
    }
    val name = location.state as? String

    if(name == null) {
        return@FC ChooseName {
            onSubmit = {
                navigate("", object : NavigateOptions {
                    override var replace: Boolean? = false
                    override var state: Any? = it
                })
            }
        }
    } else {
        return@FC LoadingScreen {
            this.name = name
            this.lobby = lobby
        }
    }

}

external interface ChooseNameProps: Props {
    var onSubmit: (String) -> Unit
}

val ChooseName = FC<ChooseNameProps> { props ->
    val (name, setName) = useState("")
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
                value = name
                label = ReactNode("Enter Name")
                onChange = {
                    setName((it.target as HTMLInputElement).value)
                }

            }
            Button {
                onClick = { props.onSubmit(name) }
                variant = ButtonVariant.contained
                +"Join"
            }
        }
    }
}

val LoadingScreen = FC<LoadingScreenProps> {props ->
    val (session, setSession) = useState<WerwolfSession>()
    useEffect(props.lobby) {
        MainScope().launch {
            val newSession = WerwolfSession.join(props.lobby, props.name)
            setSession(newSession)
        }
    }
    session?.let {
        return@FC MainGame {
            this.session = session
        }
    }

    +"Loading..."

}

external interface LoadingScreenProps: Props {
    var name: String
    var lobby: String
}
