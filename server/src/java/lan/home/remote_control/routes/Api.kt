package lan.home.remote_control.routes

import io.ktor.routing.*

internal fun Routing.api() {
    input()
    clipboard()
    stats()
}
