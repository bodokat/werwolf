package chat

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ul
import react.useEffectOnce
import react.useState

val scope = MainScope()

val Chat = FC<Props> {
    var messages: List<String> by useState(emptyList())

    useEffectOnce {
        scope.launch {
            val chatSession = ChatSession.connect("test")
            chatSession.messages.collect {
                messages = messages + it
            }
        }
    }

    h1 {
        +"chat.getChat App"
    }
    ul {
        messages.forEach {
            li {
                +it
            }
        }
    }
    
}