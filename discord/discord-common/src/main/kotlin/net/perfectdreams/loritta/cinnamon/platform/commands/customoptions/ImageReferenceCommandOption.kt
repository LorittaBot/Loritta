package net.perfectdreams.loritta.cinnamon.platform.commands.customoptions

import dev.kord.common.Locale
import dev.kord.common.entity.CommandArgument
import dev.kord.common.entity.DiscordInteraction
import dev.kord.common.entity.Snowflake
import dev.kord.rest.Image
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.string
import net.perfectdreams.discordinteraktions.common.commands.options.*
import net.perfectdreams.loritta.cinnamon.common.images.URLImageReference
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.utils.ContextStringToUserInfoConverter
import net.perfectdreams.loritta.cinnamon.platform.utils.UserUtils
import kotlin.streams.toList

// ===[ OPTION ]===
@JvmInline
value class ImageReferenceIntermediaryData(val value: String) {
    suspend fun get(context: ApplicationCommandContext): URLImageReference {
        // Now check if it is a valid thing!
        // First, we will try matching via user mentions or user IDs
        val cachedUserInfo = ContextStringToUserInfoConverter.convert(
            context,
            value
        )

        if (cachedUserInfo != null) {
            val icon = UserUtils.createUserAvatarOrDefaultUserAvatar(
                Snowflake(cachedUserInfo.id.value),
                cachedUserInfo.avatarId,
                cachedUserInfo.discriminator
            )

            return URLImageReference(icon.cdnUrl.toUrl {
                this.format = Image.Format.PNG
                this.size = Image.Size.Size128
            })
        }

        if (value.startsWith("http")) {
            // It is a URL!
            // TODO: Use a RegEx to check if it is a valid URL
            return URLImageReference(value)
        }

        // It is a emote!
        // Discord emotes always starts with "<" and ends with ">"
        return if (value.startsWith("<") && value.endsWith(">")) {
            val emoteId = value.substringAfterLast(":").substringBefore(">")
            URLImageReference("https://cdn.discordapp.com/emojis/${emoteId}.png?v=1")
        } else {
            // If not, we are going to handle it as if it were a Unicode emoji
            val emoteId = value.codePoints().toList()
                .joinToString(separator = "-") { String.format("\\u%04x", it).substring(2) }
            URLImageReference("https://twemoji.maxcdn.com/2/72x72/$emoteId.png")
        }
    }
}

class ImageReferenceCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>?,
    descriptionLocalizations: Map<Locale, String>?,
    val required: Boolean
) : NameableCommandOption<ImageReferenceIntermediaryData>(name, description, nameLocalizations, descriptionLocalizations) {
    override fun register(builder: BaseInputChatBuilder) {
        builder.string(name, description) {
            this.nameLocalizations = this@ImageReferenceCommandOption.nameLocalizations?.toMutableMap()
            this.descriptionLocalizations = this@ImageReferenceCommandOption.descriptionLocalizations?.toMutableMap()
            this.required = this@ImageReferenceCommandOption.required
        }
    }

    override fun parse(
        args: List<CommandArgument<*>>,
        interaction: DiscordInteraction
    ): ImageReferenceIntermediaryData {
        val value = args.first { it.name == name }.value as String
        return ImageReferenceIntermediaryData(value)
    }
}

// ===[ BUILDER ]===
class ImageReferenceCommandOptionBuilder(
    name: String,
    description: String,
    required: Boolean
) : CommandOptionBuilder<ImageReferenceIntermediaryData, ImageReferenceIntermediaryData>(name, description, required) {
    override fun build() = ImageReferenceCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations,
        required
    )
}