package net.perfectdreams.loritta.platform.twitter.entities

import net.perfectdreams.loritta.api.entities.Member
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.MessageChannel
import net.perfectdreams.loritta.api.messages.LorittaMessage
import net.perfectdreams.loritta.platform.twitter.LorittaTwitter
import net.perfectdreams.loritta.platform.twitter.utils.extensions.reply
import twitter4j.Status

class TwitterMessageChannel(val status: Status) : MessageChannel {
    override val name: String?
        get() = "Twitter Thread"
    override val participants: List<Member>
        get() = listOf()

    override suspend fun sendMessage(message: LorittaMessage): Message {
        return TwitterMessage(
                status.reply(
                        LorittaTwitter.TWITTER,
                        message.content
                )
        )
    }

    override suspend fun sendFile(bytes: ByteArray, fileName: String, message: LorittaMessage): Message {
        return TwitterMessage(
                status.reply(
                        LorittaTwitter.TWITTER,
                        message.content
                ) {

                    if (fileName.endsWith(".mp4")) {
                        // Videos
                        val mediaChunked = LorittaTwitter.TWITTER.uploadMediaChunked(fileName, bytes.inputStream())
                        setMediaIds(mediaChunked.mediaId)
                    } else {
                        // Images
                        setMedia(fileName, bytes.inputStream())
                    }
                }
        )
    }
}