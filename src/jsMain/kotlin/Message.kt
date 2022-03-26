import kotlinx.coroutines.launch
import lobby.ToClientMessage
import lobby.ToServerMessage
import mui.material.Button
import mui.material.ButtonGroup
import mui.material.ButtonGroupVariant
import react.FC
import react.Props
import react.useContext

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

    +props.message.text
    ButtonGroup {
        variant = ButtonGroupVariant.contained
        props.message.options.forEachIndexed { index, option ->
            Button {
                +option
                onClick = {
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
}
