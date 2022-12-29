package net.perfectdreams.loritta.morenitta.utils

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * A Web Server that provides debugging facilities
 */
class DebugWebServer {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        // 3003 = 30/03, Loritta's birthday!
        // The port is 13003 because Windows seems to reserve port 3003 for other purposes
        // Reserved ports can be checked with "netsh interface ipv4 show excludedportrange protocol=tcp"
        val server = embeddedServer(Netty, 13003) {
            routing {
                get("/coroutines") {
                    val os = ByteArrayOutputStream()
                    val ps = PrintStream(os)
                    DebugProbes.dumpCoroutines(ps)
                    call.respondText(os.toString(Charsets.UTF_8))
                }
            }
        }

        server.start(false)
    }
}