package lan.home.remote_control.wrappers

import android.os.IInterface

class WindowManager(manager: IInterface) {
  private val manager: IInterface

  init {
    this.manager = manager
  }

  // method changed since this commit:
  // https://android.googlesource.com/platform/frameworks/base/+/8ee7285128c3843401d4c4d0412cd66e86ba49e3%5E%21/#F2
  val rotation: Int

    get() {
      try {
        val cls = manager::class.java

        try {
          return cls.getMethod("getRotation").invoke(manager) as Int
        } catch (e: NoSuchMethodException) {
          return cls.getMethod("getDefaultDisplayRotation").invoke(manager) as Int
        }
      } catch (e: Exception) {
        throw AssertionError(e)
      }
    }
}
