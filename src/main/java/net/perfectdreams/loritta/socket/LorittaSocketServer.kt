package net.perfectdreams.loritta.socket

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.io.readUTF8Line
import kotlinx.coroutines.io.writeStringUtf8
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.socket.network.commands.SocketCommand
import java.net.InetSocketAddress

class LorittaSocketServer(val jsonParser: JsonParser = JsonParser(), val gson: Gson = Gson()) {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    private val commands = mutableListOf<SocketCommand>()
    val mutex = Mutex()

    fun registerCommand(command: SocketCommand) {
        commands.add(command)
    }

    fun registerCommands(vararg commands: SocketCommand) {
        this.commands.addAll(commands)
    }

    fun start(port: Int) {
        // Fake Lori Server:tm:
        GlobalScope.launch {
            val server = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().bind(InetSocketAddress("127.0.0.1", port))
            logger.info("Started socket server at ${server.localAddress}")

            while (true) {
                val socket = server.accept()

                launch {
                    logger.info("Socket accepted: ${socket.remoteAddress}")

                    val input = socket.openReadChannel()
                    val output = socket.openWriteChannel(autoFlush = true)

                    try {
                        while (true) {
                            val line = input.readUTF8Line() ?: break

                            val json = jsonParser.parse(line).obj

                            logger.info("Received: $json")

                            val op = json["op"].int

                            val command = commands.firstOrNull { it.op == op }

                            if (command == null) {
                                logger.warn("Unknown command with op code $op")
                                continue
                            }

                            val result = command.process(json)
                            val uniqueId = json["uniqueId"].nullString
                            result["uniqueId"] = uniqueId

                            mutex.withLock {
                                output.writeStringUtf8(
                                        gson.toJson(result) + "\n"
                                )
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        socket.close()
                    }
                }
            }
        }
    }
}