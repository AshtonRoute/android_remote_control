package lan.home.remote_control.wrappers

import android.annotation.SuppressLint
import android.os.Build
import android.os.IInterface
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class PowerManager(manager: IInterface) {
  private val manager: IInterface
  private val isScreenOnMethod: Method

  init {
    this.manager = manager

    try {
      @SuppressLint("ObsoleteSdkInt") // we may lower minSdkVersion in the future
      val methodName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) "isInteractive" else "isScreenOn"

      isScreenOnMethod = manager::class.java.getMethod(methodName)
    } catch (e: NoSuchMethodException) {
      throw AssertionError(e)
    }
  }

  val isScreenOn: Boolean

    get() {
      try {
        return isScreenOnMethod.invoke(manager) as Boolean
      } catch (e: InvocationTargetException) {
        throw AssertionError(e)
      } catch (e: IllegalAccessException) {
        throw AssertionError(e)
      }
    }
}
