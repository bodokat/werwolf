import csstype.Display
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import lobby.ToClientMessage
import lobby.ToServerMessage
import mui.material.*
import mui.system.sx
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.onChange

val scope = MainScope()

val sessionContext = createContext<WerwolfSession>()

val Werwolf = FC<Props> {
    val lobby = useState<ConnectState>(ConnectState.Disconnected)

    LobbyInput {
        this.state = lobby
    }

    lobby.component1().let {
        if (it is ConnectState.Connected) {
            sessionContext.Provider {
                value = it.session
                Messages {}
            }
        }
    }
}


private val Messages = FC<Props> {
    val (messages, setMessages) = useState(emptyList<ToClientMessage>())
    var started by useState(false)
    val session = useContext(sessionContext)

    useEffectOnce {
        scope.launch {
            var actualMessages = emptyList<ToClientMessage>()
            session.messages.collect {
                if(it is ToClientMessage.Started) started = true
                if(it is ToClientMessage.Ended) started = false
                actualMessages = actualMessages + it
                setMessages(actualMessages)
            }
        }
    }

    Button {
        +"Start"
        if(started) disabled = true
        onClick = {
            scope.launch {
                session.send(ToServerMessage.AdminMessage.Start)
            }
        }
    }


    List {
        messages.forEach {
            ListItem {
                Message {
                    message = it
                }
            }
        }
    }
}

external interface LobbyInputProps : Props {
    var state: StateInstance<ConnectState>
}

private val LobbyInput = FC<LobbyInputProps> { props ->
    var inputText by useState("")
    val (state, setState) = props.state

    Box {
        sx {
            display = Display.flex
        }
        TextField {
            value = inputText
            onChange = {
                inputText = (it.target as HTMLInputElement).value
            }
        }
        Button {
            variant = ButtonVariant.contained
            +"Connect"
            onClick = {
                setState(ConnectState.Connecting)
                scope.launch {
                    val session = WerwolfSession.connect(inputText)
                    setState(ConnectState.Connected(session))
                }
            }
        }
    }
}

sealed class ConnectState {
    object Disconnected : ConnectState()
    object Connecting : ConnectState()
    class Connected(val session: WerwolfSession) : ConnectState()
}