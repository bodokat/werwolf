
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.html.*
import lobby.werwolfRoute

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port) {
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Delete)
            anyHost()
        }
        install(WebSockets)
        routing {
            static("/static") {
                resources()
            }
            route("/api") {
                chatRoute()
                werwolfRoute()
            }
            get("{...}") {
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
        }
    }.start(wait = true)
}

fun HTML.index() {
    head {
        title("Werwolf")
    }
    body {
        div { id = "root" }
        script(src = "/static/werwolf.js") {}
    }
}