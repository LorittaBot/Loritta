package net.perfectdreams.loritta.platform.twitter.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.twitter.LorittaTwitter
import net.perfectdreams.loritta.platform.twitter.entities.TwitterMessage
import net.perfectdreams.loritta.platform.twitter.entities.TwitterUser
import java.net.URL
import javax.imageio.ImageIO

class TwitterCommandContext(
        loritta: LorittaBot,
        command: Command<CommandContext>,
        args: List<String>,
        message: Message,
        locale: BaseLocale
) : CommandContext(loritta, command, args, message, locale) {
    override suspend fun user(argument: Int): User? {
        (message as TwitterMessage)

        val argumentAt = args.getOrNull(argument)

        if (argumentAt?.startsWith("@") == true) {
            val userMentionEntity = message.message.userMentionEntities.firstOrNull { it.screenName == argumentAt.removePrefix("@") }

            if (userMentionEntity != null)
                LorittaTwitter.TWITTER.showUser(userMentionEntity.id)?.let { return TwitterUser(it) }
        }

        return null
    }

    override suspend fun imageUrl(argument: Int, searchPreviousMessages: Int): String? {
        (message as TwitterMessage)

        val argumentAt = args.getOrNull(argument)

        if (argumentAt?.startsWith("@") == true) {
            val userMentionEntity = message.message.userMentionEntities.firstOrNull { it.screenName == argumentAt.removePrefix("@") }

            if (userMentionEntity != null)
                LorittaTwitter.TWITTER.showUser(userMentionEntity.id)?.let { return it.biggerProfileImageURL }
        }

        val mediaEntity = message.message.mediaEntities.getOrNull(argument)
        if (mediaEntity != null)
            return mediaEntity.mediaURL

        // Oof, no media entity was found
        val repliedToStatusId = message.message.inReplyToStatusId
        println("Replied Status ID is $repliedToStatusId")
        val repliedStatus = LorittaTwitter.TWITTER.showStatus(repliedToStatusId)

        if (repliedStatus != null) {
            val repliedMediaEntity = repliedStatus.mediaEntities.getOrNull(argument)
            if (repliedMediaEntity != null)
                return repliedMediaEntity.mediaURL
        }

        return null
    }

    override suspend fun image(argument: Int, searchPreviousMessages: Int, createTextAsImageIfNotFound: Boolean): Image? {
        val mediaUrl = imageUrl(argument, searchPreviousMessages)

        return if (mediaUrl != null) {
            JVMImage(ImageIO.read(URL(mediaUrl)))
        } else {
            null
        }
    }

    override suspend fun explain() {
        message.channel.sendMessage(command.labels.toString())
    }
}