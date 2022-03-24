package net.perfectdreams.loritta.cinnamon.platform.commands.options

import dev.kord.common.entity.DiscordAttachment
import dev.kord.common.entity.Snowflake
import dev.kord.rest.Image
import net.perfectdreams.discordinteraktions.common.entities.Channel
import net.perfectdreams.discordinteraktions.common.entities.Icon
import net.perfectdreams.discordinteraktions.common.entities.Role
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.images.ImageReference
import net.perfectdreams.loritta.cinnamon.common.images.URLImageReference
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.utils.ContextStringToUserInfoConverter
import kotlin.streams.toList

abstract class CommandOption<T>(
    val name: String,
    val description: StringI18nData,
    val required: Boolean
) {
    abstract suspend fun parse(reader: ArgumentReader): T
}

sealed class ChoiceableCommandOption<T, ChoiceableType>(
    name: String,
    description: StringI18nData,
    required: Boolean,
    val choices: List<CommandChoice<ChoiceableType>>,
    val autoCompleteExecutorDeclaration: AutocompleteExecutorDeclaration<ChoiceableType>?
) : CommandOption<T>(name, description, required)

// ===[ STRING ]===
class StringCommandOption<T : String?>(
    name: String,
    description: StringI18nData,
    required: Boolean,
    choices: List<CommandChoice<String>>,
    autoCompleteExecutorDeclaration: AutocompleteExecutorDeclaration<String>?
) : ChoiceableCommandOption<T, String>(name, description, required, choices, autoCompleteExecutorDeclaration) {
    override suspend fun parse(reader: ArgumentReader): T = reader.current(name) as T
}

// ===[ INTEGER ]===
class IntegerCommandOption<T : Long?>(
    name: String,
    description: StringI18nData,
    required: Boolean,
    choices: List<CommandChoice<Long>>,
    autoCompleteExecutorDeclaration: AutocompleteExecutorDeclaration<Long>?
) : ChoiceableCommandOption<T, Long>(name, description, required, choices, autoCompleteExecutorDeclaration) {
    override suspend fun parse(reader: ArgumentReader): T = reader.current(name) as T
}

// ===[ NUMBER ]===
class NumberCommandOption<T : Double?>(
    name: String,
    description: StringI18nData,
    required: Boolean,
    choices: List<CommandChoice<Double>>,
    autoCompleteExecutorDeclaration: AutocompleteExecutorDeclaration<Double>?
) : ChoiceableCommandOption<T, Double>(name, description, required, choices, autoCompleteExecutorDeclaration) {
    override suspend fun parse(reader: ArgumentReader): T = reader.current(name) as T
}

// ===[ BOOLEAN ]===
class BooleanCommandOption<T : Boolean?>(name: String, description: StringI18nData, required: Boolean) :
    CommandOption<T>(name, description, required) {
    override suspend fun parse(reader: ArgumentReader): T = reader.current(name) as T
}

// ===[ USER ]===
class UserCommandOption<T : User?>(name: String, description: StringI18nData, required: Boolean) :
    CommandOption<T>(name, description, required) {
    override suspend fun parse(reader: ArgumentReader): T = reader.current(name) as T
}

// ===[ CHANNEL ]===
class ChannelCommandOption<T : Channel?>(name: String, description: StringI18nData, required: Boolean) :
    CommandOption<T>(name, description, required) {
    override suspend fun parse(reader: ArgumentReader): T = reader.current(name) as T
}

// ===[ ROLE ]===
class RoleCommandOption<T : Role?>(name: String, description: StringI18nData, required: Boolean) :
    CommandOption<T>(name, description, required) {
    override suspend fun parse(reader: ArgumentReader): T = reader.current(name) as T
}

// Stuff that isn't present in Discord Slash Commands yet
// (After all, this CommandOptionType is based of Discord InteraKTions implementation! :3)
class StringListCommandOption(
    name: String,
    description: StringI18nData,
    val minimum: Int?,
    val maximum: Int?
) : CommandOption<List<String>>(name, description, true) {
    override suspend fun parse(reader: ArgumentReader): List<String> {
        val listsValues = reader.entries.filter { opt -> opt.key.name.startsWith(name) }

        return mutableListOf<String>().also {
            it.addAll(listsValues.map { it.value as String })
        }
    }
}

class UserListCommandOption(
    name: String,
    description: StringI18nData,
    val minimum: Int?,
    val maximum: Int?
) : CommandOption<List<User>>(name, description, true) {
    override suspend fun parse(reader: ArgumentReader): List<User> {
        val listsValues = reader.entries.filter { opt -> opt.key.name.startsWith(name) }

        return mutableListOf<User>().also {
            it.addAll(listsValues.map { it.value as User })
        }
    }
}

class ImageReferenceCommandOption(name: String, description: StringI18nData, required: Boolean) :
    CommandOption<ImageReference>(name, description, required) {
    companion object {
        private val SUPPORTED_IMAGE_EXTENSIONS = listOf(
            "png",
            "jpg",
            "jpeg",
            "bmp",
            "tiff",
            "gif"
        )
    }

    override suspend fun parse(reader: ArgumentReader): ImageReference {
        val interaKTionAttachmentArgument =
            reader.entries.firstOrNull { opt -> opt.key.name.removeSuffix("_file") == name }
        val interaKTionAvatarLinkOrEmoteArgument =
            reader.entries.firstOrNull { opt -> opt.key.name.removeSuffix("_data") == name }

        // Attachments take priority
        if (interaKTionAttachmentArgument != null) {
            val attachment = (interaKTionAttachmentArgument.value as DiscordAttachment)
            if (attachment.filename.substringAfterLast(".")
                    .lowercase() in SUPPORTED_IMAGE_EXTENSIONS
            ) return URLImageReference(attachment.url)
        } else if (interaKTionAvatarLinkOrEmoteArgument != null) {
            val value = interaKTionAvatarLinkOrEmoteArgument.value as String

            // Now check if it is a valid thing!
            // First, we will try matching via user mentions or user IDs
            val cachedUserInfo = ContextStringToUserInfoConverter.convert(
                reader.context,
                value
            )

            if (cachedUserInfo != null) {
                val icon = cachedUserInfo.avatarId?.let {
                    Icon.UserAvatar(
                        Snowflake(cachedUserInfo.id.value),
                        it
                    )
                } ?: Icon.DefaultUserAvatar(cachedUserInfo.discriminator.toInt())

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
        // If no image was found, we will try to find the first recent message in this chat
        val channelId = reader.context.interaKTionsContext.channelId
        val messages = reader.context.loritta.rest.channel.getMessages(
            channelId,
            null,
            100
        )
        try {
            // Sort from the newest message to the oldest message
            val attachmentUrl = messages.sortedByDescending { it.id.timestamp }
                .flatMap { it.attachments }
                .firstOrNull {
                    // Only get filenames ending with "image" extensions
                    it.filename.substringAfter(".")
                        .lowercase() in SUPPORTED_IMAGE_EXTENSIONS
                }?.url

            if (attachmentUrl != null) {
                // Found a valid URL, let's go!
                return URLImageReference(attachmentUrl)
            }
        } catch (e: Exception) {
            // TODO: Catch the "permission required" exception and show a nice message
            e.printStackTrace()
        }

        reader.context.fail(
            reader.context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound),
            Emotes.LoriSob
        )
    }
}
