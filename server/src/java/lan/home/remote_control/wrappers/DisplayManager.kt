package lan.home.remote_control.wrappers

import android.os.IInterface

class DisplayManager(manager: IInterface) {
  private val manager: IInterface

  data class DisplayInfo(
    val width: Int,
    val height: Int,
    val rotation: Int
  )

  init {
    this.manager = manager
  }

  val displayInfo: DisplayInfo

  get() {
    try {
      val displayInfo = manager::class.java.getMethod("getDisplayInfo", Int::class.javaPrimitiveType).invoke(manager, 0)
      val cls = displayInfo.javaClass

      return DisplayInfo(
                         width=cls.getDeclaredField("logicalWidth").getInt(displayInfo),
                         height=cls.getDeclaredField("logicalHeight").getInt(displayInfo),
                         rotation=cls.getDeclaredField("rotation").getInt(displayInfo)
                        )
    } catch (e: Exception) {
      throw AssertionError(e)
    }
  }
}
