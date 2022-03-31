package game

import api.SessionState
import api.WerwolfSession
import csstype.*
import kotlinx.browser.window
import mui.icons.material.ContentCopy
import mui.material.Box
import mui.material.IconButton
import mui.material.Typography
import mui.system.sx
import react.FC
import react.Props
import react.createContext
import utils.useFlow

val gameContext = createContext<SessionState>()
val sessionContext = createContext<WerwolfSession>()

val MainGame = FC<MainGameProps> { props ->
    val state = useFlow(props.session.state)

    sessionContext.Provider(props.session) {
        gameContext.Provider(state) {
            Box {
                sx {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                }
                Typography {
                    variant = "h6"
                    +"Verbunden mit ${window.location.href}"
                }
                IconButton {
                    ContentCopy()
                    onClick = {
                        window.navigator.clipboard.writeText(window.location.href)
                    }
                }
            }
            Box {
                sx {
                    display = Display.grid
                    gridTemplateRows = array(5.fr,4.fr)
                    gridTemplateColumns = array(3.fr,2.fr)
                }
                Box {
                    sx {
                        gridColumn = integer(1)
                        gridRowStart = integer(1)
                        gridRowEnd = integer(3)
                    }
                    Messages()
                }
                Box {
                    sx {
                        gridColumn = integer(2)
                        gridRow = integer(1)
                    }
                    Players()
                }
                Box {
                    sx {
                        gridColumn = integer(2)
                        gridRow = integer(2)
                    }
                    Settings()
                }

            }
        }
    }
}

external interface MainGameProps: Props {
    var session: WerwolfSession
}
