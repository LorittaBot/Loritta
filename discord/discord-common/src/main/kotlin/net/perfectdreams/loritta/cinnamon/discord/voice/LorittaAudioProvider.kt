package net.perfectdreams.loritta.cinnamon.discord.voice

import dev.kord.common.annotation.KordVoice
import dev.kord.voice.AudioFrame
import dev.kord.voice.AudioProvider
import kotlinx.coroutines.channels.Channel

@OptIn(KordVoice::class)
class LorittaAudioProvider(private val audioClipProviderNotificationChannel: Channel<Unit>) : AudioProvider {
    companion object {
        val SILENCE = AudioFrame(byteArrayOf()) // While Kord does have a "SILENCE", it shows the "Speaking" indicator
    }

    var audioFramesInOpusFormatQueue = Channel<ByteArray>(Channel.UNLIMITED)
    var requestedNewAudioTracks = false

    override suspend fun provide(): AudioFrame {
        val audioDataInOpusFormatTryReceive = audioFramesInOpusFormatQueue.tryReceive()
        if (audioDataInOpusFormatTryReceive.isFailure) {
            if (requestedNewAudioTracks) // We already tried requesting it, so now we will wait...
                return SILENCE

            // isFailure == empty, then we need to request moar framez!! :3
            audioClipProviderNotificationChannel.send(Unit) // Send a moar framez!! request...
            requestedNewAudioTracks = true

            // And then return SILENCE for now
            return SILENCE
        }

        // If it is closed... then why are we here?
        if (audioDataInOpusFormatTryReceive.isClosed)
            return SILENCE

        requestedNewAudioTracks = false

        return AudioFrame(audioDataInOpusFormatTryReceive.getOrNull()!!)
    }

    /**
     * Appends the [audioFramesInOpusFormat] in the current audio provider to the end of the [audioFramesInOpusFormat] queue
     *
     * @param audioFramesInOpusFormat the Opus audio frames
     */
    suspend fun queue(audioFramesInOpusFormat: List<ByteArray>) {
        for (frame in audioFramesInOpusFormat) {
            this.audioFramesInOpusFormatQueue.send(frame)
        }
    }
}