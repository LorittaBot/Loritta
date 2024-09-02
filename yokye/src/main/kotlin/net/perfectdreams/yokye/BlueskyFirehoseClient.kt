package net.perfectdreams.yokye

import com.upokecenter.cbor.CBORObject
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant

class BlueskyFirehoseClient {
    companion object {
        private val client = HttpClient(CIO) {
            install(WebSockets) {
                // Send ping every once in a while
                pingInterval = 5_000
            }
        }
        private val logger = KotlinLogging.logger {}
    }

    // var ready = MutableStateFlow(false)
    var isShuttingDown = false
    var connectionTries = 1
    var lastEventReceivedAt = Instant.now()
    var lastSequence: Long? = null
    var lastHeaderReceived: CBORObject? = null
    var lastBodyReceived: CBORObject? = null
    private var _session: ClientWebSocketSession? = null
    val session: ClientWebSocketSession
        get() = _session ?: throw RuntimeException("Session isn't connected yet!")

    /**
     * The Bluesky [BlueskyPost] stream
     *
     * **Only a single listener should listen to the channel!**
     */
    val postStream = CoroutineQueue<BlueskyPost>(Int.MAX_VALUE)

    fun connect() {
        isShuttingDown = false
        connectionTries++

        GlobalScope.launch(Dispatchers.IO) {
            logger.info { "Starting Bluesky Firehose..." }
            try {
                client.ws(
                    "wss://bsky.network/xrpc/com.atproto.sync.subscribeRepos",
                    {
                        if (lastSequence != null)
                            parameter("cursor", lastSequence)
                    }
                ) {

                    _session = this

                    // This is a hack mostly because sometimes Firehose stops sending events
                    launch {
                        // smol initial delay
                        delay(30_000)

                        while (true) {
                            logger.info { "Checking if Firehose stopped receiving events... Last event received at $lastEventReceivedAt; Last sequence: $lastSequence; Last header: $lastHeaderReceived; Last body: $lastBodyReceived" }
                            val diff = Duration.between(lastEventReceivedAt, Instant.now())
                            if (diff.seconds >= 15) {
                                logger.warn { "Stopped receiving Firehose events! Something may have gone wrong! Restarting..." }
                                this@ws.close()
                                this@ws.cancel()
                                return@launch
                            }

                            delay(5_000)
                        }
                    }

                    for (frame in incoming) {
                        try {
                            when (frame) {
                                is Frame.Binary -> {
                                    // https://atproto.com/specs/event-stream
                                    val inputStream = frame.readBytes().inputStream()
                                    val header = CBORObject.Read(inputStream)
                                    this@BlueskyFirehoseClient.lastHeaderReceived = header
                                    this@BlueskyFirehoseClient.lastBodyReceived = null
                                    val t = header.get("t").AsString()
                                    val op = header.get("op").AsInt32()
                                    lastEventReceivedAt = Instant.now()
                                    connectionTries = 0 // Reset connection tries
                                    if (op == -1) {
                                        // https://atproto.com/specs/event-stream
                                        // Something went wrong! "Streams should be closed immediately following transmitting or receiving an error frame."
                                        val error = header.get("header").AsString()
                                        val message = header.get("message").AsString()
                                        logger.warn { "A upstream error happened in the Firehose! The connection will be closed. Error: $error; Message: $message" }
                                        this.close()
                                        this.cancel()
                                        return@ws
                                    }

                                    val body = CBORObject.Read(inputStream)
                                    this@BlueskyFirehoseClient.lastBodyReceived = body

                                    if (t == "#info") {
                                        logger.info { "Received info from the Firehose stream: $body" }
                                        continue
                                    }

                                    // #identity = seems to be identity syncs (fancy handle -> did)
                                    if (t != "#commit")
                                        continue

                                    // println(objStuff)
                                    val seq = body.get("seq")
                                    if (seq != null) {
                                        lastSequence = seq.AsInt64Value()
                                    }

                                    val repo = body.get("repo")
                                    val ops = body.get("ops") ?: continue

                                    // Sometimes ops can be null, so let's check for it
                                    ops.values.forEach {
                                        val path = it.get("path")
                                        val action = it.get("action")

                                        if (path.AsString().startsWith("app.bsky.feed.post/") && action.AsString() == "create") {
                                            val postId = path.AsString().substringAfter("/")
                                            val profileId = repo.AsString()
                                            // println("Post by $profileId")
                                            val post = BlueskyPost(profileId, postId)
                                            postStream.trySend(post)
                                        }
                                    }

                                    // We technically don't need to read the blocks, unless if we want to get the post's content
                                    /* val blocks = body.get("blocks")

                                    // Blocks can be null!
                                    if (blocks != null) {
                                        val isWhatWeWant =
                                            body.get("repo").AsString() == "did:plc:tpkrh3jv67mebzcq5xdstq65"
                                        if (isWhatWeWant) {
                                            // println(blocks::class.java)
                                            // blocks.GetByteString()
                                            val result = parseCAR(blocks.GetByteString().inputStream())
                                            var isPost = false

                                            result.forEach {
                                                val type = it.get("\$type")
                                                if (type != null && type.AsString() == "app.bsky.feed.post") {
                                                    val content = it.get("text")
                                                    // if (content != null && content.AsString().contains("hello world", true)) {
                                                    isPost = true
                                                    // }
                                                }
                                            }

                                            if (isPost) {
                                                println(body)
                                                result.forEach {
                                                    println(it.toString())
                                                }
                                            }
                                        }
                                    } */
                                }

                                is Frame.Close -> {
                                    logger.info { "Received shutdown frame! $frame" }
                                    logger.info { "Shutting down the session and reconnecting..." }
                                    this.close()
                                    this.cancel()
                                    // shutdownSession()
                                    return@ws
                                }

                                else -> {
                                    logger.info { "Received strange frame! $frame" }
                                }
                            }
                        } catch (e: Exception) {
                            logger.warn(e) { "Exception while reading frames" }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.warn(e) { "Exception while connecting to the WebSocket" }
            }

            val delay = (Math.pow(2.0, connectionTries.toDouble()) * 1_000).toLong()

            logger.warn { "WebSocket disconnected for some reason...? Reconnecting after ${delay}ms!" }
            // shutdownSession()
            delay(delay)
            connect()
        }
    }

    /* suspend fun shutdownSession() {
        isShuttingDown = true
        // ready.value = false
        if (session.isActive)
            try {
                session.close()
            } catch (e: Exception) {
                logger.warn(e) { "Failed to close session! Is it already closed?" }
            }
    } */
}