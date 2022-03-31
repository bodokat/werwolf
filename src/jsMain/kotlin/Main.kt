import kotlinx.browser.document
import react.create
import react.dom.render

fun main() {
    val root = document.getElementById("root") ?: error("No root element found")
    render(App.create(),root)
}
