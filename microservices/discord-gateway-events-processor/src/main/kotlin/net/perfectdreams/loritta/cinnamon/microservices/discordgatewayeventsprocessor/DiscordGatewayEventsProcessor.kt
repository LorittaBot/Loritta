package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Event
import dev.kord.gateway.Gateway
import dev.kord.voice.VoiceConnection
import io.ktor.client.plugins.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.gatewayproxy.GatewayProxy
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.gatewayproxy.GatewayProxyEvent
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.gatewayproxy.GatewayProxyEventWrapper
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.*
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.*
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.metrics.DiscordGatewayEventsProcessorMetrics
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.voice.LorittaAudioProvider
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.voice.LorittaVoiceConnection
import net.perfectdreams.loritta.cinnamon.platform.LorittaDiscordStuff
import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.PrometheusPushClient
import net.perfectdreams.loritta.cinnamon.platform.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.LorittaNotificationListener
import java.io.File
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class DiscordGatewayEventsProcessor(
    val config: RootConfig,
    services: Pudding,
    val guildCreateServices: Pudding,
    val languageManager: LanguageManager,
    val replicaId: Int
) : LorittaDiscordStuff(config.discord, services) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val starboardModule = StarboardModule(this)
    private val addFirstToNewChannelsModule = AddFirstToNewChannelsModule(this)
    private val discordCacheModule = DiscordCacheModule(this)
    private val bomDiaECiaModule = BomDiaECiaModule(this)
    private val debugGatewayModule = DebugGatewayModule(this)
    private val owoGatewayModule = OwOGatewayModule(this)
    private val inviteBlockerModule = InviteBlockerModule(this)
    private val afkModule = AFKModule(this)

    private val soundboard = Soundboard()

    // This is executed sequentially!
    val modules = listOf(
        discordCacheModule,
        inviteBlockerModule,
        afkModule,
        addFirstToNewChannelsModule,
        starboardModule,
        owoGatewayModule,
        debugGatewayModule
    )

    val bomDiaECia = BomDiaECia(this)
    val random = SecureRandom()
    val activeEvents = ConcurrentLinkedQueue<Job>()

    val prometheusPushClient = PrometheusPushClient("discordgatewayeventsprocessor", config.prometheusPush.url)

    val voiceConnections = ConcurrentHashMap<Snowflake, LorittaVoiceConnection>()
    private val voiceConnectionsMutexes = ConcurrentHashMap<Snowflake, Mutex>()

    private val onMessageReceived: (GatewayProxyEventWrapper) -> (Unit) = {
        val (eventType, discordEvent) = parseEvent(it.data)

        DiscordGatewayEventsProcessorMetrics.gatewayEventsReceived.labels(it.shardId.toString(), eventType).inc()

        // We will call a method that doesn't reference the "discordEventAsJsonObject" nor the "it" object, this makes it veeeery clear to the JVM that yes, you can GC the "discordEventAsJsonObject" and "it" objects
        // (Will it really GC the object? idk, but I hope it will)
        launchEventProcessorJob(
            GatewayProxyEventContext(
                eventType,
                discordEvent,
                it.shardId,
                it.receivedAt
            )
        )
    }

    val gatewayProxies = config.gatewayProxies.filter { it.replicaId == replicaId }.map {
        GatewayProxy(it.url, it.authorizationToken, it.minShard, it.maxShard, onMessageReceived)
    }

    val notificationListener = LorittaNotificationListener(services)
        .apply {
            this.start()
        }

    // This needs to be initialized AFTER the gatewayProxies above
    val tasks = DiscordGatewayEventsProcessorTasks(this)

    fun start() {
        gatewayProxies.forEachIndexed { index, gatewayProxy ->
            logger.info { "Starting Gateway Proxy $index (${gatewayProxy.url})" }
            gatewayProxy.start()
        }

        tasks.start()

        GlobalScope.launch {
            notificationListener.notifications
                .collect {
                    // We need to launch it on a separate coroutine to avoid "blocking"
                    GlobalScope.launch {
                        when (it) {
                            is FalatronVoiceRequest -> {
                                falatronStuff(it)
                            }
                            is LorittaVoiceConnectionStateRequest -> {
                                val guildId = Snowflake(it.guildId)
                                // If this instance doesn't handle this instance, let's ignore it
                                if (!isGuildHandledByThisInstance(guildId))
                                    return@launch

                                val voiceConnection = voiceConnections[guildId]

                                services.notify(
                                    LorittaVoiceConnectionStateResponse(
                                        it.uniqueId,
                                        voiceConnection?.channelId?.toLong(),
                                        voiceConnection?.isPlaying() ?: false
                                    )
                                )
                            }
                            is SoundboardAudioRequest -> {
                                val guildId = Snowflake(it.guildId)
                                // If this instance doesn't handle this instance, let's ignore it
                                if (!isGuildHandledByThisInstance(guildId))
                                    return@launch

                                val voiceConnection = getOrCreateVoiceConnection(
                                    getProxiedKordGatewayForGuild(guildId)!!,
                                    Snowflake(it.guildId),
                                    Snowflake(it.channelId)
                                )

                                val opusFrames = soundboard.getAudioClip(it.audio)

                                voiceConnection.queue(
                                    LorittaVoiceConnection.AudioClipInfo(
                                        opusFrames,
                                        Snowflake(it.channelId)
                                    )
                                )
                            }
                            else -> {}
                        }
                    }
                }
        }

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                runBlocking {
                    voiceConnections.values.forEach {
                        // Shutdown all voice connections
                        it.shutdown()
                    }
                }
            }
        )
        // bomDiaECia.startBomDiaECiaTask()
    }

    private fun parseEvent(discordGatewayEvent: GatewayProxyEvent): Pair<String, Event?> {
        val discordEventAsJsonObject = discordGatewayEvent.event
        val eventType = discordEventAsJsonObject["t"]?.jsonPrimitive?.content ?: "UNKNOWN"
        val discordEvent = KordDiscordEventUtils.parseEventFromJsonObject(discordEventAsJsonObject)

        return Pair(eventType, discordEvent)
    }

    @OptIn(ExperimentalTime::class)
    private fun launchEventProcessorJob(context: GatewayProxyEventContext) {
        if (context.event != null) {
            val coroutineName = "Event ${context.event::class.simpleName}"
            launchEventJob(coroutineName, context.durations) {
                try {
                    val proxiedKordGateway = getProxiedKordGatewayForShard(context.shardId) ?: error("Received gateway event for ${context.shardId}, but we don't have a ProxiedKordGateway for it! Bug?")
                    proxiedKordGateway.events.emit(context.event)

                    for (module in modules) {
                        val (result, duration) = measureTimedValue { module.processEvent(context) }
                        context.durations[module::class] = duration
                        DiscordGatewayEventsProcessorMetrics.executedModuleLatency
                            .labels(module::class.simpleName!!, context.eventType)
                            .observe(duration.toDouble(DurationUnit.SECONDS))

                        when (result) {
                            ModuleResult.Cancel -> {
                                // Module asked us to stop processing the events
                                return@launchEventJob
                            }
                            ModuleResult.Continue -> {
                                // Module asked us to continue processing the events
                            }
                        }
                    }
                } catch (e: Throwable) {
                    logger.warn(e) { "Something went wrong while trying to process $coroutineName! We are going to ignore..." }
                }
            }
        } else
            logger.warn { "Unknown Discord event received ${context.eventType}! We are going to ignore the event... kthxbye!" }
    }

    private fun launchEventJob(coroutineName: String, durations: Map<KClass<*>, Duration>, block: suspend CoroutineScope.() -> Unit) {
        val start = System.currentTimeMillis()

        val job = GlobalScope.launch(
            CoroutineName(coroutineName),
            block = block
        )

        activeEvents.add(job)
        DiscordGatewayEventsProcessorMetrics.activeEvents.set(activeEvents.size.toDouble())

        // Yes, the order matters, since sometimes the invokeOnCompletion would be invoked before the job was
        // added to the list, causing leaks.
        // invokeOnCompletion is also invoked even if the job was already completed at that point, so no worries!
        job.invokeOnCompletion {
            activeEvents.remove(job)
            DiscordGatewayEventsProcessorMetrics.activeEvents.set(activeEvents.size.toDouble())

            val diff = System.currentTimeMillis() - start
            if (diff >= 60_000) {
                logger.warn { "Coroutine $job ($coroutineName) took too long to process! ${diff}ms - Module Durations: $durations" }
            }
        }
    }

    private suspend fun falatronStuff(notification: FalatronVoiceRequest) {
        // Is this for us?
        val proxiedKordGateway = getProxiedKordGatewayForGuild(Snowflake(notification.guildId)) ?: run {
            logger.info { "Received a Falatron Request for Guild ID ${notification.guildId}, but we don't have its shard here! Ignoring..." }
            return
        }

        services.notify(FalatronVoiceRequestReceivedResponseX(notification.uniqueId))

        // First: Request Falatron voice
        val falatron = Falatron(config.falatron.url)
        val generatedAudioInMP3Format = try {
            falatron.generate(
                notification.voice,
                notification.text
            )
        } catch (e: Exception) {
            if (e is IllegalStateException || e is HttpRequestTimeoutException) {
                e.printStackTrace()

                // We tried ok
                services.notify(FalatronOfflineErrorResponse(notification.uniqueId))
                return
            }
            throw e
        }

        // Second: Convert the audio
        val generatedAudioInOGGFormat = convertAudio(generatedAudioInMP3Format)

        // Third: Load the OGG data from the generated audio
        val packets = soundboard.extractOpusFrames(generatedAudioInOGGFormat)

        val lorittaVoiceConnection = try {
            getOrCreateVoiceConnection(
                proxiedKordGateway,
                Snowflake(notification.guildId),
                Snowflake(notification.channelId)
            )
        } catch (e: Exception) {
            // Welp, something went wrong
            services.notify(FailedToConnectToVoiceChannelResponse(notification.uniqueId))
            return
        }

        // We don't need to switch channels here because Loritta will already switch channels when playing the audio clip
        services.notify(FalatronVoiceResponse(notification.uniqueId, lorittaVoiceConnection.isPlaying()))

        // Now we queue the audio packets
        lorittaVoiceConnection.queue(
            LorittaVoiceConnection.AudioClipInfo(
                packets,
                Snowflake(notification.channelId)
            )
        )
    }

    private fun convertAudio(byteArray: ByteArray): ByteArray {
        val processBuilder = ProcessBuilder(
            "D:\\Tools\\ffmpeg\\ffmpeg.exe",
            // "-hide_banner",
            // "-loglevel",
            // "error",
            "-f",
            "mp3",
            "-i",
            "-", // We will write to output stream
            "-ar",
            "48000",
            "-c:a",
            "libopus",
            "-ac",
            "2",
            "-f",
            "ogg",
            "-"
        ).redirectError(File("D:\\Tools\\ffmpeg\\ffmpeg_log.txt")).start()

        val inputStream = processBuilder.inputStream
        val outputStream = processBuilder.outputStream

        val input = mutableListOf<Byte>()
        // We can't use "readAllBytes" because ffmpeg stops writing to the InputStream until we read more things from it
        thread {
            while (true) {
                val value = inputStream.read()
                if (value == -1)
                    break
                input.add(value.toByte())
            }
        }

        outputStream.write(byteArray)
        outputStream.close()

        processBuilder.waitFor()

        return input.toByteArray()
    }

    /**
     * Checks if this [DiscordGatewayEventsProcessor] handles gateway events related to the guild with ID [guildId].
     *
     * @param guildId the shard ID
     * @return if any of the gateway proxies in this instance handles gateway events related to the [guildId]
     */
    fun isGuildHandledByThisInstance(guildId: Snowflake) = isShardHandledByThisInstance(getShardIdFromGuildId(guildId.toLong()))

    /**
     * Checks if this [DiscordGatewayEventsProcessor] handles gateway events related to the shard with ID [shardId].
     *
     * @param shardId the shard ID
     * @return if any of the gateway proxies in this instance handles gateway events related to the [shardId]
     */
    fun isShardHandledByThisInstance(shardId: Int) = gatewayProxies.any {
        shardId in it.minShard..it.maxShard
    }

    /**
     * Gets a Proxied Kord Gateway connection related to the [guildId], by converting the [guildId] into a Shard ID
     *
     * @param guildId the guild's ID
     * @return a proxied gateway connection, or null if this instance does not handle the [guildId]
     */
    fun getProxiedKordGatewayForGuild(guildId: Snowflake) = getProxiedKordGatewayForShard(getShardIdFromGuildId(guildId.toLong()))

    /**
     * Gets a Proxied Kord Gateway connection for the [shardId]
     *
     * @param shardId the shard's ID
     * @return a proxied gateway connection, or null if this instance does not handle the [shardId]
     */
    fun getProxiedKordGatewayForShard(shardId: Int) = gatewayProxies
        .asSequence()
        .flatMap { it.proxiedKordGateways.asSequence() }
        .firstOrNull {
            it.key == shardId
        }?.value

    /**
     * Gets or creates a [LorittaVoiceConnection] on the [guildId] and [channelId]
     *
     * @param gateway the gateway connection
     * @param guildId the guild's ID
     * @param channelId the channel's ID
     * @return a [LorittaVoiceConnection] instance
     */
    @OptIn(KordVoice::class)
    suspend fun getOrCreateVoiceConnection(
        gateway: Gateway,
        guildId: Snowflake,
        channelId: Snowflake
    ): LorittaVoiceConnection {
        voiceConnectionsMutexes.getOrPut(guildId) { Mutex() }.withLock {
            val lorittaVoiceConnection = voiceConnections[guildId]
            if (lorittaVoiceConnection != null)
                return lorittaVoiceConnection

            val notificationChannel = Channel<Unit>()

            // TODO: Send a UpdateVoiceState to disconnect Loritta from any voice channel, useful if our cache doesn't match the "reality"
            val audioProvider = LorittaAudioProvider(notificationChannel)

            val vc = VoiceConnection(
                gateway,
                Snowflake(config.discord.applicationId),
                channelId,
                guildId
            ) {
                audioProvider(audioProvider)
            }

            vc.connect()

            val loriVC = LorittaVoiceConnection(gateway, guildId, channelId, vc, audioProvider, notificationChannel)
            voiceConnections[guildId] = loriVC

            loriVC.launchAudioClipRequestsJob()

            // Clean up voice connection after it is shutdown
            vc.scope.launch {
                try {
                    awaitCancellation()
                } finally {
                    shutdownVoiceConnection(guildId, loriVC)
                }
            }

            return loriVC
        }
    }

    /**
     * Shutdowns the [VoiceConnection] and removes it from the [voiceConnections] map
     *
     * While [VoiceConnection] has a [VoiceConnection.shutdown] method, *it shouldn't be used directly to avoid memory leaks*!
     *
     * @param guildId the guild ID
     * @param voiceConnection the voice connection that will be shutdown
     */
    suspend fun shutdownVoiceConnection(guildId: Snowflake, voiceConnection: LorittaVoiceConnection) {
        logger.info { "Shutting down voice connecion $voiceConnection related to $guildId" }
        voiceConnections.remove(guildId, voiceConnection)
        voiceConnection.shutdown()
    }

    /**
     * Gets a Discord Shard ID from the provided Guild ID
     *
     * @return the shard ID
     */
    private fun getShardIdFromGuildId(id: Long): Int {
        val maxShard = config.totalShards
        return (id shr 22).rem(maxShard).toInt()
    }
}