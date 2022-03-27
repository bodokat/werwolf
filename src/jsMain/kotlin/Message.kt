import kotlinx.coroutines.launch
import lobby.ToClientMessage
import lobby.ToServerMessage
import mui.material.Button
import mui.material.ButtonGroup
import mui.material.ButtonGroupVariant
import mui.material.ButtonVariant
import react.FC
import react.Props
import react.useContext
import react.useState

external interface MessageProps : Props {
    var message: ToClientMessage
}

val Message = FC<MessageProps> { props ->
    props.message.let { message ->

        when (message) {
            is ToClientMessage.Text -> TextMessage {this.message = message}
            is ToClientMessage.Question -> ButtonMessage {this.message = message}
            else -> {}
        }
    }
}


external interface TextMessageProps: Props {
    var message: ToClientMessage.Text
}

val TextMessage = FC<TextMessageProps> { props ->
    +props.message.text
}

external interface ButtonMessageProps: Props {
    var message: ToClientMessage.Question
}

val ButtonMessage = FC<ButtonMessageProps> { props ->
    val session = useContext(sessionContext)
    var chosen: Int? by useState()

    +props.message.text
    if(chosen == null) {
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
    } } else {
        Button {
            variant = ButtonVariant.contained
            disabled = true
            +props.message.options[chosen!!]
        }
    }
}
