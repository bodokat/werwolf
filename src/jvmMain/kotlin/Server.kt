import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.html.*
import lobby.werwolfRoute

fun HTML.index() {
    head {
        title("Werwolf")
    }
    body {
        div { id = "root" }
        script(src = "/static/werwolf.js") {}
    }
}

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port) {
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Delete)
            anyHost()

        }
        install(Compression) {
            gzip()
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