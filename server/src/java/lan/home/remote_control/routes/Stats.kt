package lan.home.remote_control.routes

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.response.*
import io.ktor.http.*

import android.os.Build
import android.view.KeyEvent

import lan.home.remote_control.wrappers.ServiceManager
import lan.home.remote_control.wrappers.DisplayManager.DisplayInfo

private data class Stats(
  val deviceName: String,
  val isScreenOn: Boolean,
  val rotation: Int,
  val display: DisplayInfo
)

private val serviceManager = ServiceManager()

// val helloWorld = call.receive<HelloWorld>()

internal fun Routing.stats() {
    get("/stats") {
      val powerMgr = serviceManager.getPowerManager()
      val windowMgr = serviceManager.getWindowManager()
      val displayMgr = serviceManager.getDisplayManager()

      val displayInfo = displayMgr.displayInfo

      val stats = Stats(
          deviceName =  Build.MODEL,
          isScreenOn = powerMgr.isScreenOn,
          rotation = windowMgr.rotation,
          display = displayInfo
        )

      call.respond(stats)
    }
}
