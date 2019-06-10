package net.perfectdreams.loritta.socket

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.isClosed
import kotlinx.coroutines.*
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.writeStringUtf8
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.socket.network.SocketOpCode
import net.perfectdreams.loritta.utils.Constants
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.collections.set
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SocketWrapper(
    val socket: Socket,
    val input: ByteReadChannel,
    val output: ByteWriteChannel
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    var ping = -1L

    val _requests = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .removalListener { k1: UUID?, v1: (Pair<((ObjectNode) -> Unit), ((Throwable) -> Unit)>)?, removalCause ->
            if (removalCause == RemovalCause.EXPIRED && k1 != null && v1 != null) {
                LorittaSocket.logger.warn("Request $k1 timed out after 5 seconds...")
                v1.second.invoke(TimeoutException())
            }
        }
        .build<UUID, Pair<((ObjectNode) -> Unit), ((Throwable) -> Unit)>>()

    val cleanUpTask = GlobalScope.launch {
        while (true) {
            _requests.cleanUp()
            delay(1_000)
        }
    }
    var heartBeatTask: Job? = null

    fun close() {
        cleanUpTask.cancel()
        heartBeatTask?.cancel()
        requests.values.forEach {
            it.second.invoke(TimeoutException())
        }
        _requests.invalidateAll()
        socket.close()
    }

    val requests = _requests.asMap()

    private val mutex = Mutex()
    private val requestQueue = ConcurrentLinkedQueue<ObjectNode>()
    private val queueDispatcher = Executors.newFixedThreadPool(1)
        .asCoroutineDispatcher()

    suspend fun processQueue() {
        // logger.info { "Processing request queue, size: ${requestQueue.size}"}

        if (socket.isClosed)
            return // Not ready yet

        val output = output ?: return // Not ready yet

        while (!requestQueue.isEmpty()) {
            val request = requestQueue.poll()

            logger.info { "Sending $request via socket!"}

            try {
                mutex.withLock {
                    output.writeStringUtf8(Constants.JSON_MAPPER.writeValueAsString(request) + "\n")
                }
            } catch (e: Exception) {
                LorittaSocket.logger.error(e) { "Error while writing to socket, is the socket closed? ${socket?.isClosed}" }
                LorittaSocket.logger.warn("Readding failed request to the request queue")
                requestQueue.add(request)
            }
        }
    }

    fun sendRequestAsync(op: Int, data: ObjectNode, success: (ObjectNode) -> Unit, failure: (Throwable) -> Unit) {
        val uniqueId = UUID.randomUUID()

        data.put("op", op)
        data.put("uniqueId", uniqueId.toString())

        requests[uniqueId] = Pair(success, failure)

        requestQueue.add(data)
        GlobalScope.launch(queueDispatcher) {
            processQueue()
        }
    }

    suspend fun awaitResponse(op: Int, data: ObjectNode): ObjectNode {
        return kotlin.coroutines.suspendCoroutine { cont ->
            GlobalScope.launch(Dispatchers.IO) {
                sendRequestAsync(op, data, {
                    cont.resume(it)
                }, {
                    cont.resumeWithException(it)
                })
            }
        }
    }

    fun setUpHeartbeat() {
        heartBeatTask = GlobalScope.launch {
            while (true) {
                val start = System.currentTimeMillis()
                awaitResponse(
                    SocketOpCode.HEARTBEAT,
                    JsonNodeFactory.instance.objectNode()
                        .put("heartbeat", start)
                )
                ping = System.currentTimeMillis() - start
                logger.info("Heartbeat received, ping is ${ping}ms")
                delay(2_500)
            }
        }
    }
}