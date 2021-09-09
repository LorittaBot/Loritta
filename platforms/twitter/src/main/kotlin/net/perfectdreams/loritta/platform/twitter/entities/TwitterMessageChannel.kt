package net.perfectdreams.loritta.cinnamon.platform.twitter.entities

import blue.starry.penicillin.core.exceptions.PenicillinTwitterMediaProcessingFailedError
import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.endpoints.Media
import blue.starry.penicillin.endpoints.media
import blue.starry.penicillin.endpoints.media.MediaCategory
import blue.starry.penicillin.endpoints.media.MediaComponent
import blue.starry.penicillin.endpoints.media.MediaType
import blue.starry.penicillin.endpoints.media.uploadMedia
import blue.starry.penicillin.endpoints.media.uploadStatus
import blue.starry.penicillin.endpoints.statuses
import blue.starry.penicillin.endpoints.statuses.create
import blue.starry.penicillin.models.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import net.perfectdreams.loritta.cinnamon.common.entities.LorittaMessage
import net.perfectdreams.loritta.cinnamon.common.entities.MessageChannel
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

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
        // https://github.com/StarryBlueSky/Penicillin/issues/34
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
            ).execute().awaitProcessing()
        }.map { it.mediaId }

        client.statuses.create(
            builder.toString(),
            status.id,
            mediaIds = mediaIds
        ).execute()
    }

    // https://github.com/StarryBlueSky/Penicillin/blob/dev/src/commonMain/kotlin/blue/starry/penicillin/extensions/endpoints/CreateTweetWithMedia.kt#L76
    @OptIn(ExperimentalTime::class)
    private val mediaProcessTimeout = 60.seconds
    @OptIn(ExperimentalTime::class)
    private val defaultCheckAfter = 5.seconds

    /**
     * Awaits until media processing is done, and returns [Media] response.
     * This operation is suspendable.
     *
     * @param timeout Timeout value.
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun blue.starry.penicillin.models.Media.awaitProcessing(timeout: Duration? = null): blue.starry.penicillin.models.Media {
        if (processingInfo == null) {
            return this
        }


        var result = this

        withTimeout(timeout ?: mediaProcessTimeout) {
            while (true) {
                delay(result.processingInfo?.checkAfterSecs?.seconds ?: defaultCheckAfter)

                val response = client.media.uploadStatus(mediaId, mediaKey).execute()
                result = response.result

                if (result.processingInfo?.error != null && result.processingInfo?.state == blue.starry.penicillin.models.Media.ProcessingInfo.State.Failed) {
                    throw PenicillinTwitterMediaProcessingFailedError(result.processingInfo?.error!!, response.request, response.response)
                }

                if (result.processingInfo == null || result.processingInfo?.state == blue.starry.penicillin.models.Media.ProcessingInfo.State.Succeeded) {
                    break
                }
            }
        }

        return result
    }
}