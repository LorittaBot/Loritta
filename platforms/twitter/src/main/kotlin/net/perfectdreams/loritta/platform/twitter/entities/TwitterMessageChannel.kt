package net.perfectdreams.loritta.platform.twitter.entities

import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.endpoints.media
import blue.starry.penicillin.endpoints.media.MediaCategory
import blue.starry.penicillin.endpoints.media.MediaComponent
import blue.starry.penicillin.endpoints.media.MediaType
import blue.starry.penicillin.endpoints.media.uploadMedia
import blue.starry.penicillin.endpoints.statuses
import blue.starry.penicillin.endpoints.statuses.create
import blue.starry.penicillin.models.Status
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.MessageChannel

class TwitterMessageChannel(private val client: ApiClient, private val status: Status) : MessageChannel {
    override suspend fun sendMessage(message: LorittaMessage) {
        val builder = StringBuilder()

        // Required, if not it won't show up as a reply!
        builder.append("@${status.user.screenName} ")
        message.content?.let {
            builder.append(it)
            builder.append("\n")
        }

        for (reply in message.replies) {
            builder.append(reply.content)
            builder.append("\n")
        }

        // If there is a file, we will upload the media and then tweet with the IDs
        // This is from the "createWithMedia" method from Penicillin
        // However we need to use the "inReplyToStatusId so that's why we don't use "createWithMedia"
        val mediaIds = message.files.entries.map { (fileName, data) ->
            // TODO: Type should be inferred from the message itself
            // It would be nice if there was a "ContentType" for file send

            val type: MediaType
            val category: MediaCategory

            val extension = fileName.substringAfterLast(".")
            when (extension) {
                "jpg", "jpeg" -> {
                    type = MediaType.JPEG
                    category = MediaCategory.TweetImage
                }
                "png" -> {
                    type = MediaType.PNG
                    category = MediaCategory.TweetImage
                }
                "gif" -> {
                    type = MediaType.GIF
                    category = MediaCategory.TweetGif
                }
                "mp4" -> {
                    type = MediaType.MP4
                    category = MediaCategory.TweetVideo
                }
                else -> throw UnsupportedOperationException("Unsupported file type! $extension")
            }

            client.media.uploadMedia(
                MediaComponent(
                    data,
                    type,
                    category
                )
            ).execute()
        }.map { it.mediaId }

        client.statuses.create(
            builder.toString(),
            inReplyToStatusId = status.id,
            mediaIds = mediaIds
        ).execute()
    }
}