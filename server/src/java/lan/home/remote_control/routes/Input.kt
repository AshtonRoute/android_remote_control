package lan.home.remote_control.routes

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.http.*

import android.os.Build
import android.view.KeyEvent
import android.view.InputDevice

import lan.home.remote_control.wrappers.ServiceManager

private val SOURCES = object: HashMap<String, Int>() {
  init {
    put("keyboard", InputDevice.SOURCE_KEYBOARD)
    put("dpad", InputDevice.SOURCE_DPAD)
    put("gamepad", InputDevice.SOURCE_GAMEPAD)
    put("touchscreen", InputDevice.SOURCE_TOUCHSCREEN)
    put("mouse", InputDevice.SOURCE_MOUSE)
    put("stylus", InputDevice.SOURCE_STYLUS)
    put("trackball", InputDevice.SOURCE_TRACKBALL)
    put("touchpad", InputDevice.SOURCE_TOUCHPAD)
    put("touchnavigation", InputDevice.SOURCE_TOUCH_NAVIGATION)
    put("joystick", InputDevice.SOURCE_JOYSTICK)
  }
}

private data class KeyCode(
  val code: Int?,
  val codeName: String?,

  val sourceCode: Int?,
  val sourceCodeName: String?,

  val longpress: Boolean = false,
  val repeat: Int = 0
)

private data class Tap(
  val sourceCode: Int?,
  val sourceCodeName: String?,

  val x: Float,
  val y: Float
)

private data class Move(
  val sourceCode: Int?,
  val sourceCodeName: String?,

  val dx: Float,
  val dy: Float
)

private data class Swipe(
  val sourceCode: Int?,
  val sourceCodeName: String?,

  val x1: Float,
  val y1: Float,

  val x2: Float,
  val y2: Float,

  val duration: Int
)

private data class Text(
  val message: String
)

private val serviceManager = ServiceManager()

private fun awakeIfScreenOff() {
  val powerMgr = serviceManager.getPowerManager()

  if (powerMgr.isScreenOn) return

  val inputMgr = serviceManager.getInputManager()

  inputMgr.sendKeyEvent(InputDevice.SOURCE_KEYBOARD, KeyEvent.KEYCODE_POWER)
}

private fun getSourceCode(sourceCode: Int?, sourceCodeName: String?) : Int {
  var curSourceCode = InputDevice.SOURCE_KEYBOARD

  if (sourceCode != null) {
    curSourceCode = sourceCode
  } else if (sourceCodeName != null) {
    val curSourceCodeName = sourceCodeName.toUpperCase()
    val matchedCode = SOURCES.get(curSourceCodeName)

    if (matchedCode != null) {
      curSourceCode = matchedCode
    }
  }

  return curSourceCode
}

private fun getKeyCode(code: Int?, codeName: String?) : Int {
  var curCode = KeyEvent.KEYCODE_UNKNOWN

  if (code != null) {
    curCode = code
  } else if (codeName != null) {
    val curCodeName = codeName.toUpperCase()
    curCode = KeyEvent.keyCodeFromString(curCodeName)

    if (curCode == KeyEvent.KEYCODE_UNKNOWN) {
      curCode = KeyEvent.keyCodeFromString("KEYCODE_${curCodeName}")
    }
  }

  return curCode
}

internal fun Routing.input() {
    post("/keycode") {
      val inputMgr = serviceManager.getInputManager()

      val msg = call.receive<KeyCode>()

      var curSourceCode = getSourceCode(msg.sourceCode, msg.sourceCodeName)

      if (curSourceCode == InputDevice.SOURCE_UNKNOWN) {
        return@post call.respond(HttpStatusCode.BadRequest, "Unknown source code received: (sourceCode: ${msg.sourceCode}, sourceCodeName: ${msg.sourceCodeName})")
      }

      val curCode = getKeyCode(msg.code, msg.codeName)

      if (curCode == KeyEvent.KEYCODE_UNKNOWN) {
        return@post call.respond(HttpStatusCode.BadRequest, "Unknown code received: (code: ${msg.code}, codeName: ${msg.codeName})")
      }

      awakeIfScreenOff()
      inputMgr.sendKeyEvent(curSourceCode, curCode, msg.longpress, msg.repeat)

      call.respond(HttpStatusCode.OK)
    }

    post("/wake") {
      awakeIfScreenOff()

      call.respond(HttpStatusCode.OK)
    }


    post("/text") {
      val inputMgr = serviceManager.getInputManager()

      val msg = call.receive<Text>()

      awakeIfScreenOff()
      inputMgr.sendText(msg.message)

      call.respond(HttpStatusCode.OK)
    }

    post("/pasteFromClipboard") {
      val inputMgr = serviceManager.getInputManager()
      val clipBMgr = serviceManager.getClipboardManager()

      val curText = clipBMgr.text

      if (!curText.isEmpty()) {
        awakeIfScreenOff()
        inputMgr.sendText(curText)
      }

      call.respond(HttpStatusCode.OK)
    }

    post("/move") {
      val inputMgr = serviceManager.getInputManager()

      val msg = call.receive<Move>()

      var curSourceCode = getSourceCode(msg.sourceCode, msg.sourceCodeName)

      if (curSourceCode == InputDevice.SOURCE_UNKNOWN) {
        return@post call.respond(HttpStatusCode.BadRequest, "Unknown source code received: (sourceCode: ${msg.sourceCode}, sourceCodeName: ${msg.sourceCodeName})")
      }

      awakeIfScreenOff()
      inputMgr.sendMove(curSourceCode, msg.dx, msg.dy)

      call.respond(HttpStatusCode.OK)
    }

    post("/tap") {
      val inputMgr = serviceManager.getInputManager()

      val msg = call.receive<Tap>()

      var curSourceCode = getSourceCode(msg.sourceCode, msg.sourceCodeName)

      if (curSourceCode == InputDevice.SOURCE_UNKNOWN) {
        return@post call.respond(HttpStatusCode.BadRequest, "Unknown code received: (sourceCode: ${msg.sourceCode}, sourceCodeName: ${msg.sourceCodeName})")
      }

      awakeIfScreenOff()
      inputMgr.sendTap(curSourceCode, msg.x, msg.y)

      call.respond(HttpStatusCode.OK)
    }

    post("/swipe") {
      val inputMgr = serviceManager.getInputManager()

      val msg = call.receive<Swipe>()

      var curSourceCode = getSourceCode(msg.sourceCode, msg.sourceCodeName)

      if (curSourceCode == InputDevice.SOURCE_UNKNOWN) {
        return@post call.respond(HttpStatusCode.BadRequest, "Unknown source code received: (sourceCode: ${msg.sourceCode}, sourceCodeName: ${msg.sourceCodeName})")
      }

      awakeIfScreenOff()
      inputMgr.sendSwipe(curSourceCode, msg.x1, msg.y1, msg.x2, msg.y2, msg.duration)

      call.respond(HttpStatusCode.OK)
    }
}
