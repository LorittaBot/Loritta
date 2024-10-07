package net.perfectdreams.loritta.lorituber.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.*
import net.perfectdreams.loritta.lorituber.server.processors.Processors
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask
import net.perfectdreams.loritta.serializable.lorituber.requests.*
import net.perfectdreams.loritta.serializable.lorituber.responses.GetChannelsByCharacterResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.GetCharactersByOwnerResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.GetServerInfoResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.LoriTuberRPCResponse
import org.jetbrains.exposed.sql.*
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class LoriTuberServer(val pudding: Pudding) {
    companion object {
        // For comparisons:
        // The Sims 1: one in game minute = one real life second
        // The Sims Online: one in game minute = five real life seconds
        private const val TICKS_PER_SECOND = 1
        private const val TICK_DELAY = 1_000
        private val logger = KotlinLogging.logger {}
        private const val GENERAL_INFO_KEY = "general"
    }

    var currentTick = 0L
    var lastUpdate = System.currentTimeMillis()
    val mutex = Mutex()
    val pendingGameLoopRequests = mutableListOf<PendingGameLoopRequest>()
    val processors = Processors()
    val averageTickDurations = mutableListOf<Duration>()

    fun start() {
        runBlocking {
            pudding.transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    LoriTuberCharacters,
                    LoriTuberServerInfos,
                    LoriTuberChannels,
                    LoriTuberMails,
                    LoriTuberPendingVideos
                )
            }

            pudding.transaction {
                LoriTuberServerInfos.select { LoriTuberServerInfos.type eq GENERAL_INFO_KEY }
                    .firstOrNull()
                    ?.get(LoriTuberServerInfos.data)
                    ?.let { Json.decodeFromString<ServerInfo>(it) }
                    ?.also {
                        currentTick = it.currentTick
                        lastUpdate = it.lastUpdate
                    }
            }
        }

        thread(name = "LoriTuber Game Loop") {
            gameLoop()
        }

        embeddedServer(Netty, port = 8080) {
            routing {
                get("/") {
                    call.respondText("LoriTuber Server")
                }

                get("/servers/{serverName}") {
                    call.respondText(
                        Json.encodeToString(
                            GetServerInfoResponse(
                                currentTick,
                                lastUpdate,
                                averageTickDurations.map { it.inWholeMilliseconds }.average()
                            )
                        ),
                        ContentType.Application.Json
                    )
                }

                post("/rpc") {
                    // TODO: How can we "unhook" the tick rate from things that do not affect the game state?
                    val request = Json.decodeFromString<LoriTuberRPCRequest>(call.receiveText())
                    val channel = Channel<LoriTuberRPCResponse>()

                    val pendingRequest = PendingGameLoopRequest(
                        request,
                        channel
                    )

                    logger.info { "Received $request" }

                    if (pendingRequest.request is GetServerInfoRequest) {
                        call.respondText(
                            Json.encodeToString<LoriTuberRPCResponse>(
                                GetServerInfoResponse(
                                    currentTick,
                                    lastUpdate,
                                    averageTickDurations.map { it.inWholeMilliseconds }.average()
                                )
                            ),
                            ContentType.Application.Json
                        )
                        return@post
                    }

                    mutex.withLock {
                        pendingGameLoopRequests.add(pendingRequest)
                    }
                    val response = channel.receive()

                    logger.info { "Sending $response" }

                    call.respondText(
                        Json.encodeToString(response),
                        ContentType.Application.Json
                    )
                }
            }
        }.start(wait = true)
    }

    private fun gameLoop() {
        runBlocking {
            while (true) {
                val beginProcessingTicksTime = System.currentTimeMillis()

                // This allows the game to "catch up"
                while (beginProcessingTicksTime > lastUpdate + TICK_DELAY) {
                    val timeUpdateDiff = beginProcessingTicksTime - lastUpdate
                    val ticksBehind = timeUpdateDiff / TICK_DELAY
                    println("Current Tick: $currentTick - Last Update Time: $lastUpdate - Ticks Behind: $ticksBehind")

                    val start = System.currentTimeMillis()

                    // Process pending game loop requests
                    val pendingGameLoopRequests = mutex.withLock {
                        val newList = pendingGameLoopRequests.toList()
                        pendingGameLoopRequests.clear()
                        newList
                    }

                    pudding.transaction {
                        for (gameLoopRequest in pendingGameLoopRequests) {
                            // TODO: If this causes an exception, catch it and throw the error on the responseChannel (or close it?) to avoid it being suspended indefinitely
                            // TODO: Don't stop the game loop if an exception happens
                            gameLoopRequest.responseChannel.send(
                                when (val request = gameLoopRequest.request) {
                                    is CreateCharacterRequest -> processors.createCharacterRequestProcessor.process(
                                        request,
                                        currentTick,
                                        lastUpdate
                                    )

                                    is GetServerInfoRequest -> GetServerInfoResponse(
                                        currentTick,
                                        lastUpdate,
                                        averageTickDurations.map { it.inWholeMilliseconds }.average()
                                    )

                                    is GetCharactersByOwnerRequest -> {
                                        GetCharactersByOwnerResponse(
                                            currentTick,
                                            lastUpdate,
                                            LoriTuberCharacters.select { LoriTuberCharacters.owner eq request.ownerId }
                                                .map {
                                                    GetCharactersByOwnerResponse.LoriTuberCharacter(
                                                        it[LoriTuberCharacters.id].value,
                                                        it[LoriTuberCharacters.name]
                                                    )
                                                }
                                        )
                                    }

                                    is GetCharacterStatusRequest -> processors.getCharacterStatusRequestProcessor.process(
                                        request,
                                        currentTick,
                                        lastUpdate
                                    )

                                    is CreateChannelRequest -> processors.createChannelRequestProcessor.process(
                                        request,
                                        currentTick,
                                        lastUpdate
                                    )

                                    is GetChannelByIdRequest -> processors.getChannelByIdRequestProcessor.process(
                                        request,
                                        currentTick,
                                        lastUpdate
                                    )

                                    is GetChannelsByCharacterRequest -> {
                                        GetChannelsByCharacterResponse(
                                            currentTick,
                                            lastUpdate,
                                            LoriTuberChannels.select { LoriTuberChannels.owner eq request.characterId }
                                                .map {
                                                    LoriTuberChannel(
                                                        it[LoriTuberChannels.id].value,
                                                        it[LoriTuberChannels.name]
                                                    )
                                                }
                                        )
                                    }

                                    is GetMailRequest -> processors.getMailRequestProcessor.process(request, currentTick, lastUpdate)
                                    is AcknowledgeMailRequest -> processors.acknowledgeMailRequestProcessor.process(request, currentTick, lastUpdate)

                                    is StartTaskRequest -> processors.startTaskRequestProcessor.process(request, currentTick, lastUpdate)
                                    is CancelTaskRequest -> processors.cancelTaskRequestProcessor.process(request, currentTick, lastUpdate)

                                    is CreatePendingVideoRequest -> processors.createPendingVideoRequestProcessor.process(request, currentTick, lastUpdate)

                                    is GetPendingVideosByChannelRequest -> processors.getPendingVideosByChannelRequestProcessor.process(request, currentTick, lastUpdate)
                                }
                            )
                        }

                        // Tick each player
                        val characters = LoriTuberCharacters
                            .selectAll()

                        for (character in characters) {
                            var isSleeping = false
                            val currentTaskAsJson = character[LoriTuberCharacters.currentTask]

                            if (currentTaskAsJson != null) {
                                when (val task = Json.decodeFromString<LoriTuberTask>(currentTaskAsJson)) {
                                    is LoriTuberTask.Sleeping -> isSleeping = true
                                    is LoriTuberTask.WorkingOnVideo -> {
                                        val pendingVideoData = LoriTuberPendingVideos.select {
                                            LoriTuberPendingVideos.id eq task.pendingVideoId
                                        }.firstOrNull()

                                        // Is energy depleted?
                                        // TODO: Hunger
                                        if (character[LoriTuberCharacters.energyNeed] == 0.0) {
                                            // If yes, we will reset the current task
                                            LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                                it[LoriTuberCharacters.currentTask] = null
                                            }
                                        } else {
                                            // Increment the video progress
                                            LoriTuberPendingVideos.update({ LoriTuberPendingVideos.id eq task.pendingVideoId }) {
                                                with(SqlExpressionBuilder) {
                                                    it[LoriTuberPendingVideos.renderingProgress] = LoriTuberPendingVideos.renderingProgress + 1.0
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Every 5s we are going to decrease their motives
                            if (currentTick % (TICKS_PER_SECOND * 5) == 0L) {
                                LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                    it[LoriTuberCharacters.hungerNeed] = (character[LoriTuberCharacters.hungerNeed] - 1.0).coerceAtLeast(0.0)

                                    if (isSleeping) {
                                        it[LoriTuberCharacters.energyNeed] = (character[LoriTuberCharacters.energyNeed] + 1.0).coerceIn(0.0, 100.0)
                                    } else {
                                        it[LoriTuberCharacters.energyNeed] = (character[LoriTuberCharacters.energyNeed] - 1.0).coerceAtLeast(0.0)
                                    }
                                }
                            }
                        }

                        LoriTuberServerInfos.upsert(LoriTuberServerInfos.type) {
                            it[LoriTuberServerInfos.type] = GENERAL_INFO_KEY
                            it[LoriTuberServerInfos.data] = Json.encodeToString(
                                ServerInfo(
                                    currentTick,
                                    lastUpdate
                                )
                            )
                        }
                    }

                    val end = System.currentTimeMillis()
                    val diff = end - start
                    if (averageTickDurations.size == ((TICK_DELAY * TICKS_PER_SECOND) * 60)) {
                        averageTickDurations.removeAt(0)
                    }
                    averageTickDurations.add(diff.milliseconds)

                    currentTick++

                    lastUpdate += TICK_DELAY
                }

                lastUpdate = System.currentTimeMillis()

                val timeToWait = TICK_DELAY - (System.currentTimeMillis() - beginProcessingTicksTime)

                println("Waiting ${timeToWait}ms")
                if (timeToWait > 0)
                    Thread.sleep(timeToWait)
                else
                    println("Can't keep up!")
            }
        }
    }

    @Serializable
    data class ServerInfo(
        val currentTick: Long,
        val lastUpdate: Long
    )

    class PendingGameLoopRequest(
        val request: LoriTuberRPCRequest,
        val responseChannel: Channel<LoriTuberRPCResponse>
    )
}