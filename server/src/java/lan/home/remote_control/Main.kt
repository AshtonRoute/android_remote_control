package lan.home.remote_control

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.server.netty.Netty
import io.ktor.server.engine.embeddedServer
import org.slf4j.event.*

import java.io.File
import java.io.IOException

import kotlin.system.exitProcess

import lan.home.remote_control.routes.api

data class Options(
  val port: Int,
  val tlsCertsPath: String
)

object Main {
  @Throws(IOException::class)
  private fun init(options: Options) {
    startServer(options)
  }

  private fun startServer(options: Options) {
    // TODO: add https support using options.tlsCertsPath

    embeddedServer(Netty, port = options.port,
      module = Application::main
    ).start(wait = true)
  }

  private fun createOptions(args: Array<String>) : Options? {
    var port = 32436
    var tlsCertsPath = ""

    try {
      port = args[0].toInt()
    } catch (e: NumberFormatException) {
      Log.e("Invalid port specified: ${args[0]}", e)

      return null
    }

    if (args.size > 1) {
      tlsCertsPath = args[1]

      val file = File(tlsCertsPath)

      if (!file.exists()) {
        Log.e("Certificate file doesn't exist: ${tlsCertsPath}")

        return null
      }
    }

    val options = Options(port = port, tlsCertsPath = tlsCertsPath)

    return options
  }

  private fun unlinkSelf() {
    try {
      File(System.getProperty("java.class.path")).delete()
    } catch (e: Exception) {
      Log.e("Can't unlink the application jar", e)
    }
  }

  @Throws(Exception::class)
  @JvmStatic fun main(args: Array<String>) {
    Thread.setDefaultUncaughtExceptionHandler(object: Thread.UncaughtExceptionHandler {
      public override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e("Exception on thread ${t}", e)

        exitProcess(1)
      }
    })

    unlinkSelf()
    val options = createOptions(args)

    if (options == null) {
      exitProcess(1)
    }

    init(options)
  }
}

  fun Application.main() {
    install(DefaultHeaders)
    install(Compression)
    install(XForwardedHeaderSupport)

    install(CallLogging) {
      level = Level.INFO
    }

    // install(HttpsRedirect) {
    //     sslPort = 443
    // }

    install(ContentNegotiation) {
      gson {}
    }

    install(Routing) {
      api()
    }
  }
