package net.perfectdreams.loritta.cinnamon.discord.voice

import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.managers.AudioManager
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import kotlin.time.Duration.Companion.minutes

data class LorittaVoiceConnection(
    private val guild: Guild,
    var channelId: Long, // Users can move Loritta to another channel
    private val audioManager: AudioManager,
    private val audioProvider: LorittaAudioProvider,
    private val audioClipProviderNotificationChannel: Channel<Unit>
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val audioClips = Channel<AudioClipInfo>(Channel.UNLIMITED)
    private val scope = CoroutineScope(SupervisorJob())
    private var detachMutex = Mutex()
    private var detachJob: Job? = null

    fun isPlaying() = !audioProvider.audioFramesInOpusFormatQueue.isEmpty

    suspend fun queue(audioClip: AudioClipInfo) {
        audioClips.send(audioClip)
    }

    suspend fun switchChannel(channelId: Long) {
        if (this.channelId != channelId) {
            this.channelId = channelId
            audioManager.openAudioConnection(guild.getVoiceChannelById(channelId.toLong()))
        }
    }

    suspend fun shutdown() {
        audioManager.closeAudioConnection()
        scope.cancel()
        try {
            audioClips.close()
        } catch (_: ClosedReceiveChannelException) {} // Ignore if it is was already closed
    }

    fun launchAudioClipRequestsJob() = scope.launch {
        for (notification in audioClipProviderNotificationChannel) {
            // So you want new audio frames, huh? Okaay :3
            logger.info { "Received a \"moar framez!! :3\" request on the audio channel, let's try getting more audio clips (if possible...)" }

            // Initialize automatic shutdown after 5 minutes of inactivity
            detachJob?.cancel()

            detachMutex.withLock {
                detachJob = scope.launch {
                    delay(5.minutes)
                    logger.info { "Shutting down connection ${audioManager} due to inactivity... Bye!" }
                    shutdown() // Technically the connection will be cleaned up from the voice connections map after the voice connection is shutdown...
                }
            }

            val audioClipInfo = audioClips.receive() // This will suspend if there isn't any request audio clips in the channel
            detachJob?.cancel()

            if (channelId != audioClipInfo.channelId)
                switchChannel(audioClipInfo.channelId) // Audio Clip was queued on a different channel, switch to it!

            audioProvider.queue(audioClipInfo.frames)
        }
    }

    class AudioClipInfo(
        val frames: List<ByteArray>,
        val channelId: Long
    )
}