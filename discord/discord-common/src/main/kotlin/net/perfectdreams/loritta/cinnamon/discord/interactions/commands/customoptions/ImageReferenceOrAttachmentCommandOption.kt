package net.perfectdreams.loritta.cinnamon.discord.interactions.commands.customoptions

import dev.kord.common.entity.CommandArgument
import dev.kord.common.entity.DiscordAttachment
import dev.kord.common.entity.DiscordInteraction
import dev.kord.common.entity.Snowflake
import dev.kord.rest.Image
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.attachment
import dev.kord.rest.builder.interaction.string
import net.perfectdreams.discordinteraktions.common.commands.options.*
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.images.ImageReference
import net.perfectdreams.loritta.cinnamon.images.URLImageReference
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ContentTypeUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.ContextStringToUserInfoConverter
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import kotlin.streams.toList

// ===[ OPTION ]===
data class ImageReferenceOrAttachmentIntermediaryData(
    val dataValue: String?,
    val attachmentValue: DiscordAttachment?,
    val required: Boolean
) {
    suspend fun get(context: ApplicationCommandContext): net.perfectdreams.loritta.cinnamon.images.URLImageReference? {
        // Attachments take priority
        if (attachmentValue != null) {
            if (attachmentValue.contentType.value in ContentTypeUtils.COMMON_IMAGE_CONTENT_TYPES)
                return net.perfectdreams.loritta.cinnamon.images.URLImageReference(attachmentValue.url)

            // This ain't an image dawg! Because the user explicitly provided the image, then let's fail
            context.fail(
                context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound),
                Emotes.LoriSob
            )
        } else if (dataValue != null) {
            // Now check if it is a valid thing!
            // First, we will try matching via user mentions or user IDs
            val cachedUserInfo = ContextStringToUserInfoConverter.convert(
                context,
                dataValue
            )

            if (cachedUserInfo != null) {
                val icon = UserUtils.createUserAvatarOrDefaultUserAvatar(
                    Snowflake(cachedUserInfo.id.value),
                    cachedUserInfo.avatarId,
                    cachedUserInfo.discriminator
                )

                return net.perfectdreams.loritta.cinnamon.images.URLImageReference(icon.cdnUrl.toUrl {
                    this.format = Image.Format.PNG
                    this.size = Image.Size.Size128
                })
            }

            if (dataValue.startsWith("http")) {
                // It is a URL!
                // TODO: Use a RegEx to check if it is a valid URL
                return net.perfectdreams.loritta.cinnamon.images.URLImageReference(dataValue)
            }

            // It is a emote!
            // Discord emotes always starts with "<" and ends with ">"
            return if (dataValue.startsWith("<") && dataValue.endsWith(">")) {
                val emoteId = dataValue.substringAfterLast(":").substringBefore(">")
                net.perfectdreams.loritta.cinnamon.images.URLImageReference("https://cdn.discordapp.com/emojis/${emoteId}.png?v=1")
            } else {
                // If not, we are going to handle it as if it were a Unicode emoji
                val emoteId = dataValue.codePoints().toList()
                    .joinToString(separator = "-") { String.format("\\u%04x", it).substring(2) }
                net.perfectdreams.loritta.cinnamon.images.URLImageReference("https://twemoji.maxcdn.com/2/72x72/$emoteId.png")
            }
        }

        // If no image was found, we will try to find the first recent message in this chat
        val channelId = context.channelId
        try {
            val messages = context.loritta.rest.channel.getMessages(
                channelId,
                null,
                100
            )

            // Sort from the newest message to the oldest message
            val attachmentUrl = messages.sortedByDescending { it.id.timestamp }
                .flatMap { it.attachments }
                .firstOrNull {
                    // Only get filenames ending with "image" extensions
                    it.contentType.value in ContentTypeUtils.COMMON_IMAGE_CONTENT_TYPES
                }?.url

            if (attachmentUrl != null) {
                // Found a valid URL, let's go!
                return net.perfectdreams.loritta.cinnamon.images.URLImageReference(attachmentUrl)
            }
        } catch (e: Exception) {
            // TODO: Catch the "permission required" exception and show a nice message
            e.printStackTrace()
        }

        if (required) {
            context.fail(
                context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound),
                Emotes.LoriSob
            )
        }

        return null
    }
}

class ImageReferenceOrAttachmentOption(
    override val name: String,
    val required: Boolean
) : InteraKTionsCommandOption<ImageReferenceOrAttachmentIntermediaryData> {
    override fun register(builder: BaseInputChatBuilder) {
        builder.string(name + "_data", "Image, URL or Emoji") {
            this.required = false
        }

        builder.attachment(name + "_attachment", "Image Attachment") {
            this.required = false
        }
    }

    override fun parse(
        args: List<CommandArgument<*>>,
        interaction: DiscordInteraction
    ): ImageReferenceOrAttachmentIntermediaryData {
        val dataValue = args.firstOrNull { it.name == name + "_data" }?.value as String?
        val attachmentValue = args.firstOrNull { it.name == name + "_attachment" }?.value as DiscordAttachment?

        return ImageReferenceOrAttachmentIntermediaryData(
            dataValue,
            attachmentValue,
            required
        )
    }
}

// ===[ BUILDER ]===
class ImageReferenceOrAttachmentOptionBuilder(
    override val name: String,
    override val required: Boolean
) : CommandOptionBuilder<ImageReferenceOrAttachmentIntermediaryData, ImageReferenceOrAttachmentIntermediaryData>() {
    override fun build() = ImageReferenceOrAttachmentOption(
        name,
        required
    )
}