package net.perfectdreams.loritta.socket

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.readUTF8Line
import kotlinx.coroutines.io.writeStringUtf8
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import java.net.ConnectException
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.collections.set
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LorittaSocket(val port: Int) {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    val _requests = Caffeine.newBuilder()
        .expireAfterWrite(15, TimeUnit.SECONDS)
        .removalListener { k1: UUID?, v1: (Pair<((JsonObject) -> Unit), ((Throwable) -> Unit)>)?, removalCause ->
            if (removalCause == RemovalCause.EXPIRED && k1 != null && v1 != null) {
                logger.warn("Request $k1 timed out after 15 seconds...")
                v1.second.invoke(TimeoutException())
            }
        }
        .build<UUID, Pair<((JsonObject) -> Unit), ((Throwable) -> Unit)>>()

    val requests = _requests.asMap()

    var socket: Socket? = null
    var output: ByteWriteChannel? = null
    private val mutex = Mutex()
    private val requestQueue = ConcurrentLinkedQueue<JsonObject>()

    suspend fun processQueue() {
        // logger.info { "Processing request queue, size: ${requestQueue.size}"}

        if (socket?.isClosed != false)
            return // Not ready yet

        val output = output ?: return // Not ready yet

        while (!requestQueue.isEmpty()) {
            val request = requestQueue.poll()

            // logger.info { "Sending $request via socket!"}

            try {
                mutex.withLock {
                    output.writeStringUtf8(Gson().toJson(request) + "\n")
                }
            } catch (e: Exception) {
                logger.error(e) { "Error while writing to socket, is the socket closed? ${socket?.isClosed}" }
                logger.warn("Readding failed request to the request queue")
                requestQueue.add(request)

                if (socket?.isClosed == true) {
                    logger.error("Forcing socket reconnect at queue loop")
                    startSocket()
                }
            }
        }
    }

    suspend fun sendRequestAndGet(op: Int, data: JsonObject, success: (JsonObject) -> Unit, failure: (Throwable) -> Unit) {
        val uniqueId = UUID.randomUUID()

        data["op"] = op
        data["uniqueId"] = uniqueId.toString()

        requests[uniqueId] = Pair(success, failure)

        requestQueue.add(data)
        processQueue()
    }

    suspend fun sendRequestAsyncAndGet(op: Int, data: JsonObject): JsonObject {
        return kotlin.coroutines.suspendCoroutine { cont ->
            GlobalScope.launch(Dispatchers.IO) {
                sendRequestAndGet(op, data, {
                    cont.resume(it)
                }, {
                    cont.resumeWithException(it)
                })
            }
        }
    }

    fun connect() {
        startSocket()
    }

    fun startSocket() {
        GlobalScope.launch {
            while (true) {
                // Nós queremos que dê timeout mesmo que não estejamos inserindo/removendo coisas do cache
                _requests.cleanUp()
                delay(1000)
            }
        }

        GlobalScope.launch {
            val socket = try {
                aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                    .connect(InetSocketAddress("127.0.0.1", port))

            } catch (e: ConnectException) {
                logger.error("Failed to connect to socket, waiting 2.5 seconds... There are ${requestQueue.size} requests queued")
                delay(2_500)
                startSocket()
                return@launch
            }

            val input = socket.openReadChannel()

            this@LorittaSocket.socket = socket
            output = socket.openWriteChannel(autoFlush = true)

            logger.info("Socket is ready!")

            launch {
                while (true) {
                    try {
                        if (socket.isClosed) {
                            logger.warn("Socket is closed on the socket main loop, shutting down coroutine task")
                            break
                        }

                        val line = input.readUTF8Line()

                        if (line == null) {
                            logger.warn("Null string on readUTF8Line!")
                            socket.close()
                            break
                        }

                        val json = JsonParser().parse(line).obj

                        logger.info("Received: $json")

                        val uniqueIdStr = json["uniqueId"].nullString ?: continue

                        val uniqueId = UUID.fromString(uniqueIdStr)

                        val callback = requests[uniqueId]?.first

                        requests.remove(uniqueId)

                        if (callback == null) {
                            logger.warn("Unknown request $uniqueId was received!")
                        } else {
                            callback.invoke(json.obj)
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "Error while reading socket input, is socket closed? ${socket.isClosed}" }
                        break
                    }
                }

                if (socket.isClosed) {
                    logger.warn("Socket seems to be closed, reconnecting...")
                    startSocket()
                } else {
                    logger.warn("Socket left socket main loop, but doesn't seem to be closed? Ignoring...")
                }
            }

            // Processar requests pendentes (caso o socket tenha desconectado)
            processQueue()
        }
    }
}