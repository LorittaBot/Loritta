package net.perfectdreams.loritta.cinnamon.discord.voice

import kotlinx.coroutines.channels.Channel
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class LorittaAudioProvider(private val audioClipProviderNotificationChannel: Channel<Unit>) : AudioSendHandler {
    companion object {
        private val SILENCE = ByteBuffer.wrap(byteArrayOf()) // While Kord does have a "SILENCE", it shows the "Speaking" indicator
    }

    var audioFramesInOpusFormatQueue = Channel<ByteArray>(Channel.UNLIMITED)
    var requestedNewAudioTracks = false

    override fun isOpus() = true
    override fun canProvide() = true

    override fun provide20MsAudio(): ByteBuffer {
        val audioDataInOpusFormatTryReceive = audioFramesInOpusFormatQueue.tryReceive()
        if (audioDataInOpusFormatTryReceive.isFailure) {
            if (requestedNewAudioTracks) // We already tried requesting it, so now we will wait...
                return SILENCE

            // isFailure == empty, then we need to request moar framez!! :3
            audioClipProviderNotificationChannel.trySend(Unit) // Send a moar framez!! request...
            requestedNewAudioTracks = true

            // And then return SILENCE for now
            return SILENCE
        }

        // If it is closed... then why are we here?
        if (audioDataInOpusFormatTryReceive.isClosed)
            return SILENCE

        requestedNewAudioTracks = false

        return ByteBuffer.wrap(audioDataInOpusFormatTryReceive.getOrNull()!!)
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