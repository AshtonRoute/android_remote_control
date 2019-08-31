package lan.home.remote_control
import android.util.Log as AndroidLog

/**
* Log both to Android logger (so that logs are visible in "adb logcat") and standard output/error (so that they are visible in the terminal
* directly).
*/

object Log {
  private val TAG = "remote_control"
  private val PREFIX = "[server] "
  private val THRESHOLD = if (BuildConfig.DEBUG) Level.DEBUG else Level.INFO

  enum class Level {
    DEBUG,
    INFO,
    WARN,
    ERROR
  }

  fun isEnabled(level: Level) : Boolean {
    return level.ordinal >= THRESHOLD.ordinal
  }

  fun d(message: String) {
    if (isEnabled(Level.DEBUG)) {
      AndroidLog.d(TAG, message)
      println("${PREFIX}DEBUG: ${message}")
    }
  }

  fun i(message:String) {
    if (isEnabled(Level.INFO)) {
      AndroidLog.i(TAG, message)
      println("${PREFIX}INFO: ${message}")
    }
  }

  fun w(message:String) {
    if (isEnabled(Level.WARN)) {
      AndroidLog.w(TAG, message)
      println("${PREFIX}WARN: ${message}")
    }
  }

  @JvmOverloads fun e(message: String, throwable: Throwable? = null) {
    if (isEnabled(Level.ERROR)) {
      AndroidLog.e(TAG, message, throwable)
      println("${PREFIX}ERROR: ${message}")

      if (throwable != null) {
        throwable.printStackTrace()
      }
    }
  }
}
