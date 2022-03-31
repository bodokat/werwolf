import game.Game
import login.Login
import react.FC
import react.Props
import react.createElement
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter

val App = FC<Props> {
    BrowserRouter {
        Routes {
            Route {
                path = "/l/:lobby"
                element = createElement(Game)
            }
            Route {
                path = "*"
                element = createElement(Login)
            }
        }
    }
}