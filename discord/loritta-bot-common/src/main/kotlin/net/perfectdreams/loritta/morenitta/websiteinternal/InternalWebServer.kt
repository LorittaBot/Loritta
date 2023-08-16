package net.perfectdreams.loritta.morenitta.websiteinternal

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.RPCResponseException
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.Processors
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * A Web Server that provides debugging facilities and internal (not exposed to the outside world) RPC between Loritta instances
 */
class InternalWebServer(val m: LorittaBot) {
    val processors = Processors(this)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        // 3003 = 30/03, Loritta's birthday!
        // The port is 13003 because Windows seems to reserve port 3003 for other purposes
        // Reserved ports can be checked with "netsh interface ipv4 show excludedportrange protocol=tcp"
        val server = embeddedServer(CIO, 13003) {
            install(Compression)

            routing {
                post("/rpc") {
                    val body = withContext(Dispatchers.IO) { call.receiveText() }

                    val response = try {
                        when (val request = Json.decodeFromString<LorittaInternalRPCRequest>(body)) {
                            is LorittaInternalRPCRequest.GetGuildInfoRequest -> {
                                processors.getGuildInfoProcessor.process(call, request)
                            }

                            is LorittaInternalRPCRequest.GetLorittaInfoRequest -> {
                                processors.getLorittaInfoProcessor.process(call, request)
                            }

                            is LorittaInternalRPCRequest.GetGuildGamerSaferConfigRequest -> {
                                processors.getGuildGamerSaferConfigProcessor.process(call, request)
                            }

                            is LorittaInternalRPCRequest.UpdateGuildGamerSaferConfigRequest -> {
                                processors.updateGuildGamerSaferConfigProcessor.process(call, request)
                            }
                        }
                    } catch (e: RPCResponseException) {
                        e.response
                    }

                    call.respondJson(
                        Json.encodeToString<LorittaInternalRPCResponse>(response)
                    )
                }

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