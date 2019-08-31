package lan.home.remote_control.wrappers

import android.content.ClipData
import android.os.IInterface
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class ClipboardManager(manager: IInterface) {
  private val manager: IInterface
  private val getPrimaryClipMethod: Method
  private val setPrimaryClipMethod: Method

  init {
    this.manager = manager

    try {
      getPrimaryClipMethod = manager::class.java.getMethod("getPrimaryClip", String::class.java)
      setPrimaryClipMethod = manager::class.java.getMethod("setPrimaryClip", ClipData::class.java, String::class.java)
    } catch (e: NoSuchMethodException) {
      throw AssertionError(e)
    }
  }

  var text: String

    get() {
      try {
        val clipVal = getPrimaryClipMethod.invoke(manager, "com.android.shell")

        if (clipVal == null) {
          return ""
        }

        val clipData = clipVal as ClipData

        if (clipData == null || clipData.getItemCount() == 0) {
          return ""
        }

        return clipData.getItemAt(0).getText().toString()
      } catch (e: InvocationTargetException) {
        throw AssertionError(e)
      } catch (e: IllegalAccessException) {
        throw AssertionError(e)
      }
    }

    set(text) {
      val clipData = ClipData.newPlainText(null, text)

      try {
        setPrimaryClipMethod.invoke(manager, clipData, "com.android.shell")
      } catch (e: InvocationTargetException) {
        throw AssertionError(e)
      } catch (e: IllegalAccessException) {
        throw AssertionError(e)
      }
    }
}
