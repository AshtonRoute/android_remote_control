package lan.home.remote_control.wrappers

import android.annotation.SuppressLint
import android.os.IBinder
import android.os.IInterface
import java.lang.reflect.Method

@SuppressLint("PrivateApi")
class ServiceManager {
  private val getServiceMethod: Method
  private var clipboardManager: ClipboardManager? = null
  private var displayManager: DisplayManager? = null
  private var inputManager: InputManager? = null
  private var powerManager: PowerManager? = null
  private var windowManager: WindowManager? = null

  init {
    try {
      getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String::class.java)
    } catch (e: Exception) {
      throw AssertionError(e)
    }
  }

  private fun getService(service: String, type: String): IInterface {
    try {
      val binder = getServiceMethod.invoke(null, service) as IBinder
      val asInterfaceMethod = Class.forName(type + "\$Stub").getMethod("asInterface", IBinder::class.java)

      return asInterfaceMethod.invoke(null, binder) as IInterface
    } catch (e: Exception) {
      throw AssertionError(e)
    }
  }

  fun getClipboardManager(): ClipboardManager {
    if (clipboardManager == null) {
      clipboardManager = ClipboardManager(getService("clipboard", "android.content.IClipboard"))
    }

    return clipboardManager as ClipboardManager
  }

  fun getDisplayManager(): DisplayManager {
    if (displayManager == null) {
      displayManager = DisplayManager(getService("display", "android.hardware.display.IDisplayManager"))
    }

    return displayManager as DisplayManager
  }

  fun getInputManager(): InputManager {
    if (inputManager == null) {
      inputManager = InputManager(getService("input", "android.hardware.input.IInputManager"))
    }

    return inputManager as InputManager
  }

  fun getPowerManager(): PowerManager {
    if (powerManager == null) {
      powerManager = PowerManager(getService("power", "android.os.IPowerManager"))
    }

    return powerManager as PowerManager
  }

  fun getWindowManager(): WindowManager {
    if (windowManager == null) {
      windowManager = WindowManager(getService("window", "android.view.IWindowManager"))
    }

    return windowManager as WindowManager
  }
}
