package net.perfectdreams.loritta.socket

import com.fasterxml.jackson.databind.JsonNode
import com.mrpowergamerbr.loritta.utils.Constants
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.io.readUTF8Line
import kotlinx.coroutines.io.writeStringUtf8
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.socket.network.commands.SocketCommand
import net.perfectdreams.loritta.utils.extensions.obj
import net.perfectdreams.loritta.utils.extensions.objectNode
import java.net.InetSocketAddress

class LorittaSocket(val port: Int) {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    private val commands = mutableListOf<SocketCommand>()
    var socketWrapper: SocketWrapper? = null
    var onSocketDisconnect: ((Socket) -> (Unit))? = null
    var onMessageReceived: ((JsonNode) -> (Unit))? = null
    var onSocketConnected: ((SocketWrapper) -> (Unit))? = null

    val mutex = Mutex()

    fun registerCommand(command: SocketCommand) {
        commands.add(command)
    }

    fun registerCommands(vararg commands: SocketCommand) {
        this.commands.addAll(commands)
    }

    fun connect() {
        startSocket()
    }

    fun startSocket() {
        GlobalScope.launch {
            logger.info("Trying to connect to controller...")
            val socket = try {
                aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                        .connect(InetSocketAddress("127.0.0.1", port))
            } catch (e: Exception) {
                logger.warn("Couldn't connect to controller, waiting 2 seconds... and reconnecting!")
                delay(2_000)
                startSocket()
                return@launch
            }

            val input = socket.openReadChannel()
            val output = socket.openWriteChannel(autoFlush = true)

            val socketWrapper = SocketWrapper(socket, input, output)
            this@LorittaSocket.socketWrapper = socketWrapper

            logger.info("Socket is ready!")
            onSocketConnected?.invoke(socketWrapper)

            while (true) {
                val line = input.readUTF8Line()

                if (line == null) {
                    logger.warn("Line is \"null\" while reading UTF8 String! Exiting socket main loop...")
                    break
                }

                val json = Constants.JSON_MAPPER.readTree(line)

                // logger.info("Received: $json")

                if (json.has("op")) {
                    // Se existe um "op code", quer dizer que estão perguntando coisas para o website
                    val op = json["op"].intValue()

                    val command = commands.firstOrNull { it.op == op }

                    val result = if (command == null) {
                        logger.warn("Unknown command with op code $op")
                        objectNode(
                                "status" to "Unknown op code: $op"
                        )
                    } else {
                        try {
                            command.process(socketWrapper, json)
                        } catch (e: Exception) {
                            logger.error("Exception while processing $json")
                            objectNode(
                                    "status" to e.message
                            )
                        }
                    }

                    // logger.info("Before changes: $result")

                    val uniqueId = json["uniqueId"].textValue()
                    result.obj.put("uniqueId", uniqueId)

                    // logger.info("Sending: " + Constants.JSON_MAPPER.writeValueAsString(result))

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

            logger.error { "Left socket main loop! Reconnecting..." }
            startSocket()
        }
    }
}