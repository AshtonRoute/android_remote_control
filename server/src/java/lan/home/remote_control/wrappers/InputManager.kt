package lan.home.remote_control.wrappers

import android.os.IInterface
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.InputDevice
import android.view.InputEvent
import android.view.KeyCharacterMap

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class InputManager(manager: IInterface) {
  private val manager: IInterface
  private val injectInputEventMethod: Method

  companion object {
    val INJECT_INPUT_EVENT_MODE_ASYNC = 0
    val INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = 1
    val INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2
  }

  init {
    this.manager = manager

    try {
      injectInputEventMethod = manager::class.java.getMethod("injectInputEvent", InputEvent::class.java, Int::class.javaPrimitiveType)
    } catch (e: NoSuchMethodException) {
      throw AssertionError(e)
    }
  }

  fun injectInputEvent(inputEvent: InputEvent, mode: Int) : Boolean {
    try {
      return injectInputEventMethod.invoke(manager, inputEvent, mode) as Boolean
    } catch (e: InvocationTargetException) {
      throw AssertionError(e)
    } catch (e: IllegalAccessException) {
      throw AssertionError(e)
    }
  }

  fun sendText(text: String) {
    val buff = StringBuffer(text)
    var escapeFlag = false

    for (i in 0 until buff.length) {
      if (escapeFlag) {
        escapeFlag = false

        if (buff.get(i) == 's') {
          buff.setCharAt(i, ' ')

          i.dec()
          buff.deleteCharAt(i)
        }
      }

      if (buff.get(i) == '%') {
        escapeFlag = true
      }
    }

    val chars = buff.toString().toCharArray()
    val kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
    val events = kcm.getEvents(chars)

    for (i in events.indices) {
      injectKeyEvent(events[i])
    }
  }

  fun sendKeyEvent(inputSource: Int, keyCode: Int, longpress: Boolean = false, repeat: Int = 0) {
    val now = SystemClock.uptimeMillis()

    injectKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, repeat, 0,
                           KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, inputSource))

    if (longpress) {
      injectKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 1, 0,
                              KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_LONG_PRESS,
                              inputSource))
    }

    injectKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, repeat, 0,
                            KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, inputSource))
  }

  fun sendTap(inputSource: Int, x: Float, y: Float) {
    val now = SystemClock.uptimeMillis()

    injectMotionEvent(inputSource, MotionEvent.ACTION_DOWN, now, x, y, 1.0f)
    injectMotionEvent(inputSource, MotionEvent.ACTION_UP, now, x, y, 0.0f)
  }

  fun sendSwipe(inputSource: Int, x1: Float, y1: Float, x2: Float, y2: Float, duration: Int) {
    var curDuration = duration

    if (curDuration < 0) {
      curDuration = 300
    }

    var now = SystemClock.uptimeMillis()

    injectMotionEvent(inputSource, MotionEvent.ACTION_DOWN, now, x1, y1, 1.0f)

    val startTime = now
    val endTime = startTime + curDuration

    while (now < endTime) {
      val elapsedTime = now - startTime
      val alpha = elapsedTime.toFloat() / curDuration

      injectMotionEvent(inputSource, MotionEvent.ACTION_MOVE, now, lerp(x1, x2, alpha),
                        lerp(y1, y2, alpha), 1.0f)

      now = SystemClock.uptimeMillis()
    }

    injectMotionEvent(inputSource, MotionEvent.ACTION_UP, now, x1, y1, 0.0f)
  }

  fun sendMove(inputSource: Int, dx: Float, dy: Float) {
    val now = SystemClock.uptimeMillis()

    injectMotionEvent(inputSource, MotionEvent.ACTION_MOVE, now, dx, dy, 0.0f)
  }

  fun injectKeyEvent(event: KeyEvent) {
    injectInputEvent(event, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH)
  }

  fun injectMotionEvent(inputSource: Int, action: Int, `when`:Long, x: Float, y: Float, pressure: Float) {
    val DEFAULT_SIZE = 1.0f
    val DEFAULT_META_STATE = 0
    val DEFAULT_PRECISION_X = 1.0f
    val DEFAULT_PRECISION_Y = 1.0f
    val DEFAULT_DEVICE_ID = 0
    val DEFAULT_EDGE_FLAGS = 0

    val event = MotionEvent.obtain(`when`, `when`, action, x, y, pressure, DEFAULT_SIZE,
                                   DEFAULT_META_STATE, DEFAULT_PRECISION_X, DEFAULT_PRECISION_Y, DEFAULT_DEVICE_ID,
                                   DEFAULT_EDGE_FLAGS)

    event.setSource(inputSource)

    injectInputEvent(event, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH)
  }

  fun lerp(a: Float, b: Float, alpha: Float) : Float {
    return (b - a) * alpha + a
  }
}
