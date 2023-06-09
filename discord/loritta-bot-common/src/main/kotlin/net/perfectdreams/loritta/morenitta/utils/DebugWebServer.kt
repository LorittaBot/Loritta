package net.perfectdreams.loritta.morenitta.utils

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import net.perfectdreams.loritta.morenitta.LorittaBot
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * A Web Server that provides debugging facilities
 */
class DebugWebServer(val m: LorittaBot) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        // 3003 = 30/03, Loritta's birthday!
        // The port is 13003 because Windows seems to reserve port 3003 for other purposes
        // Reserved ports can be checked with "netsh interface ipv4 show excludedportrange protocol=tcp"
        val server = embeddedServer(CIO, 13003) {
            routing {
                // Dumps all currently running coroutines
                get("/coroutines") {
                    val os = ByteArrayOutputStream()
                    val ps = PrintStream(os)
                    DebugProbes.dumpCoroutines(ps)
                    call.respondText(os.toString(Charsets.UTF_8))
                }

                // Dumps all pending messages on the event queue
                get("/pending-messages") {
                    val coroutinesInfo = DebugProbes.dumpCoroutinesInfo()

                    val os = ByteArrayOutputStream()
                    val ps = PrintStream(os)

                    m.pendingMessages.forEach {
                        ps.println(DebugProbes.jobToString(it).removeSuffix("\n"))

                        val info = coroutinesInfo.firstOrNull { info -> info.job == it }
                        if (info != null) {
                            for (frame in info.lastObservedStackTrace()) {
                                ps.println("\t$frame")
                            }
                        }

                        ps.println()
                    }

                    call.respondText(os.toString(Charsets.UTF_8))
                }
            }
        }

        server.start(false)
    }
}