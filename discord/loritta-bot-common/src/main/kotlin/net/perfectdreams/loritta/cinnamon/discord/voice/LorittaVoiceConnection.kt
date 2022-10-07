package net.perfectdreams.loritta.cinnamon.discord.voice

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Gateway
import dev.kord.gateway.UpdateVoiceStatus
import dev.kord.voice.VoiceConnection
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import kotlin.time.Duration.Companion.minutes

@OptIn(KordVoice::class)
data class LorittaVoiceConnection(
    private val gateway: DeviousGateway,
    private val guildId: Snowflake,
    var channelId: Snowflake, // Users can move Loritta to another channel
    private val voiceConnection: VoiceConnection,
    private val audioProvider: LorittaAudioProvider,
    private val audioClipProviderNotificationChannel: Channel<Unit>
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val audioClips = Channel<AudioClipInfo>(Channel.UNLIMITED)
    private val scope = CoroutineScope(SupervisorJob())
    private var detachJob: Job? by atomic(null)

    fun isPlaying() = !audioProvider.audioFramesInOpusFormatQueue.isEmpty

    suspend fun queue(audioClip: AudioClipInfo) {
        audioClips.send(audioClip)
    }

    suspend fun switchChannel(channelId: Snowflake) {
        if (this.channelId != channelId) {
            gateway.kordGateway.send(
                UpdateVoiceStatus(
                    guildId = guildId,
                    channelId = channelId,
                    selfMute = false,
                    selfDeaf = false
                )
            )
        }
    }

    suspend fun shutdown() {
        voiceConnection.shutdown()
        scope.cancel()
    }

    fun launchAudioClipRequestsJob() = scope.launch {
        for (notification in audioClipProviderNotificationChannel) {
            // So you want new audio frames, huh? Okaay :3
            logger.info { "Received a \"moar framez!! :3\" request on the audio channel, let's try getting more audio clips (if possible...)" }

            // Initialize automatic shutdown after 5 minutes of inactivity
            detachJob?.cancel()
            detachJob = scope.launch {
                delay(5.minutes)
                logger.info { "Shutting down connection $voiceConnection due to inactivity... Bye!" }
                shutdown() // Technically the connection will be cleaned up from the voice connections map after the voice connection is shutdown...
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
        val channelId: Snowflake
    )
}