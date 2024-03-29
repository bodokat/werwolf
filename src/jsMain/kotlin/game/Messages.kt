package game

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import lobby.ToClientMessage
import lobby.ToServerMessage
import mui.material.*
import react.*

val scope = MainScope()

val Messages = FC<Props> {
    val state = useContext(gameContext)

    List {
        state.messages.forEach {
            message(it)
        }
    }
}

fun ChildrenBuilder.message(message: ToClientMessage) {

    when(message) {
        is ToClientMessage.Initial -> {
            println("Warning: Got Initial message again")
            return
        }
        is ToClientMessage.Joined -> ListItem { +"${message.player} joined" }
        is ToClientMessage.Left -> ListItem {  +"${message.player} left" }
        ToClientMessage.Started -> ListItem { +"Game started" }
        is ToClientMessage.Text -> ListItem { +message.text }
        is ToClientMessage.Question -> ButtonMessage {
            this.message = message
        }
        ToClientMessage.Ended -> ListItem { +"Game ended" }
        is ToClientMessage.NewSettings -> return
    }
}


val ButtonMessage = FC<ButtonMessageProps> { props ->
    val session = useContext(sessionContext)
    var chosen: Int? by useState()
    ListItem {
        +props.message.text
        if (chosen == null) {
            ButtonGroup {
                variant = ButtonGroupVariant.contained
                props.message.options.forEachIndexed { index, option ->
                    Button {
                        +option
                        onClick = {
                            chosen = index
                            scope.launch {
                                session.send(
                                    ToServerMessage.Response(
                                        id = props.message.id, choice = index
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Button {
                variant = ButtonVariant.contained
                disabled = true
                +props.message.options[chosen!!]
            }
        }
    }
}

external interface ButtonMessageProps: Props {
    var message: ToClientMessage.Question
}
