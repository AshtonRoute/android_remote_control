package lan.home.remote_control.routes

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.http.*

import lan.home.remote_control.wrappers.ServiceManager

private data class Clipboard(
  val value: String
)

private val serviceManager = ServiceManager()

internal fun Routing.clipboard() {
    get("/clipboard") {
      val clipBMgr = serviceManager.getClipboardManager()

      val clipboard = Clipboard(value = clipBMgr.text)

      call.respond(clipboard)
    }

    post("/clipboard") {
      val clipBMgr = serviceManager.getClipboardManager()

      val msg = call.receive<Clipboard>()

      clipBMgr.text = msg.value

      call.respond(HttpStatusCode.OK)
    }
}
