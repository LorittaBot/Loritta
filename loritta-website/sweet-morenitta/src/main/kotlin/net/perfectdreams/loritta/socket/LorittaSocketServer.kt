package net.perfectdreams.loritta.socket

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
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
import net.perfectdreams.loritta.utils.Constants
import net.perfectdreams.loritta.utils.extensions.obj
import net.perfectdreams.loritta.utils.extensions.set
import java.net.InetSocketAddress

class LorittaSocketServer(private val port: Int) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val commands = mutableListOf<SocketCommand>()
    var onSocketDisconnect: ((Socket) -> (Unit))? = null
    var onMessageReceived: ((JsonNode) -> (Unit))? = null

    val mutex = Mutex()

    fun registerCommand(command: SocketCommand) {
        commands.add(command)
    }

    fun registerCommands(vararg commands: SocketCommand) {
        this.commands.addAll(commands)
    }

    fun start() {
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

                    val socketWrapper = SocketWrapper(socket, input, output)

                    try {
                        while (true) {
                            val line = input.readUTF8Line()

                            if (line == null) {
                                logger.warn("Line is \"null\" while reading UTF8 String! Exiting socket main loop...")
                                break
                            }

                            val json = Constants.JSON_MAPPER.readTree(line)

                            logger.info("Received: $json")

                            if (json.has("op")) {
                                // Se existe um "op code", quer dizer que estão perguntando coisas para o website
                                val op = json["op"].intValue()

                                val command = commands.firstOrNull { it.op == op }

                                if (command == null) {
                                    logger.warn("Unknown command with op code $op")
                                    continue
                                }

                                val result = command.process(socketWrapper, json)
                                // logger.info("Before changes: $result")

                                val uniqueId = json["uniqueId"].textValue()
                                result.obj["uniqueId"] = uniqueId

                                logger.info("Sending: " + Constants.JSON_MAPPER.writeValueAsString(result))

                                mutex.withLock {
                                    output.writeStringUtf8(
                                        Constants.JSON_MAPPER.writeValueAsString(result) + "\n"
                                    )
                                }
                            } else {
                                // Se não existir um "op code", quer dizer que é callback de alguma coisa
                                onMessageReceived?.invoke(json)
                            }
                        }

                        throw RuntimeException("Left socket main loop!")
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        onSocketDisconnect?.invoke(socket)
                        socket.close()
                    }
                }
            }
        }
    }
}