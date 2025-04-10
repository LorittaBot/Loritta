package net.perfectdreams.loritta.morenitta.interactions.commands.options

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.ContentTypeUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete.AutocompleteExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReference.Companion.getImageUrl
import kotlin.streams.toList

sealed class OptionReference<T>(
    val name: String
)

sealed class DiscordOptionReference<T>(
    name: String,
    val description: StringI18nData,
    val required: Boolean
) : OptionReference<T>(name) {
    abstract fun get(option: OptionMapping): T
}

class StringDiscordOptionReference<T>(name: String, description: StringI18nData, required: Boolean) : DiscordOptionReference<T>(name, description, required) {
    val choices = mutableListOf<Choice>()
    var autocompleteExecutor: AutocompleteExecutor<T>? = null

    fun choice(name: String, value: String) = choices.add(Choice.RawChoice(name, value))
    fun choice(name: StringI18nData, value: String) = choices.add(Choice.LocalizedChoice(name, value))

    fun autocomplete(executor: AutocompleteExecutor<T>) {
        this.autocompleteExecutor = executor
    }

    override fun get(option: OptionMapping): T {
        return option.asString as T
    }

    sealed class Choice {
        class LocalizedChoice(
            val name: StringI18nData,
            val value: String
        ) : Choice()

        class RawChoice(
            val name: String,
            val value: String
        ) : Choice()
    }
}

class BooleanDiscordOptionReference<T>(name: String, description: StringI18nData, required: Boolean) : DiscordOptionReference<T>(name, description, required) {
    override fun get(option: OptionMapping): T {
        return option.asBoolean as T
    }
}

class LongDiscordOptionReference<T>(
    name: String,
    description: StringI18nData,
    required: Boolean,
    val requiredRange: LongRange?
) : DiscordOptionReference<T>(name, description, required) {
    var autocompleteExecutor: AutocompleteExecutor<T>? = null

    fun autocomplete(executor: AutocompleteExecutor<T>) {
        this.autocompleteExecutor = executor
    }

    override fun get(option: OptionMapping): T {
        return option.asLong as T
    }
}

class NumberDiscordOptionReference<T>(
    name: String,
    description: StringI18nData,
    required: Boolean,
    val requiredRange: ClosedFloatingPointRange<Double>?
) : DiscordOptionReference<T>(name, description, required) {
    var autocompleteExecutor: AutocompleteExecutor<T>? = null

    fun autocomplete(executor: AutocompleteExecutor<T>) {
        this.autocompleteExecutor = executor
    }

    override fun get(option: OptionMapping): T {
        return option.asDouble as T
    }
}

class ChannelDiscordOptionReference<T>(name: String, description: StringI18nData, required: Boolean) : DiscordOptionReference<T>(name, description, required) {
    override fun get(option: OptionMapping): T = option.asChannel as T
}

class RoleDiscordOptionReference<T>(name: String, description: StringI18nData, required: Boolean) : DiscordOptionReference<T>(name, description, required) {
    override fun get(option: OptionMapping): T = option.asRole as T
}

class UserDiscordOptionReference<T>(name: String, description: StringI18nData, required: Boolean) : DiscordOptionReference<T>(name, description, required) {
    override fun get(option: OptionMapping): T {
        val user = option.asUser
        val member = option.asMember

        return UserAndMember(
            user,
            member
        ) as T
    }
}

data class UserAndMember(
    val user: User,
    val member: Member?
)

class AttachmentDiscordOptionReference<T>(name: String, description: StringI18nData, required: Boolean) : DiscordOptionReference<T>(name, description, required) {
    override fun get(option: OptionMapping): T {
        return option.asAttachment as T
    }
}

class ImageReferenceDiscordOptionReference<T>(name: String) : OptionReference<T>(name)

class ImageReference(val dataValue: String) {
    companion object {
        /**
         * Gets an image URL from the [context] and [dataValue]
         *
         * @param context       the context
         * @param dataValue     the data value of the command
         * @param searchHistory if the channel history should be searched to find other images
         * @return an image URL string, of null if nothing was found
         */
        internal suspend fun getImageUrl(
            context: UnleashedContext,
            dataValue: String?,
            searchHistory: Boolean
        ): String? {
            if (dataValue != null) {
                if (dataValue.startsWith("http")) {
                    // It is a URL!
                    // TODO: Use a RegEx to check if it is a valid URL
                    return dataValue
                }

                // It is a user mention!
                if (dataValue.startsWith("<@") && dataValue.endsWith(">")) {
                    val userIdAsString = dataValue.removePrefix("<@").removeSuffix(">")
                    val userId = userIdAsString.toLongOrNull()

                    // If it is a valid long (so, it must be a snowflake...)
                    if (userId != null) {
                        // And if the user is present in the mentions list...
                        val user = context.mentions.users.firstOrNull { it.idLong == userId }
                        if (user != null)
                        // Then yay! Get the user avatar!!
                            return user.effectiveAvatarUrl
                    }
                }

                // It is a emote!
                // Discord emotes always starts with "<" and ends with ">"
                if (dataValue.startsWith("<") && dataValue.endsWith(">")) {
                    val emoteId = dataValue.substringAfterLast(":").substringBefore(">")
                    return "https://cdn.discordapp.com/emojis/${emoteId}.png?v=1"
                }

                // It is a unicode emoji!
                if (context.loritta.unicodeEmojiManager.regex.matches(dataValue)) {
                    val emoteId = dataValue.codePoints().toList()
                        .joinToString(separator = "-") { String.format("\\u%04x", it).substring(2) }
                    return "https://abs.twimg.com/emoji/v2/72x72/$emoteId.png"
                }
            }

            if (searchHistory) {
                // If no image was found, we will try to find the first recent message in this chat
                val channel = context.channelOrNull
                val guild = context.guildOrNull

                // Only non-detached channels have the ability of querying the channel history
                if (channel != null && !channel.isDetached) {
                    val canQuery = if (guild != null && channel is GuildChannel) {
                        guild.selfMember.hasPermission(channel, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND)
                    } else true

                    if (canQuery) {
                        val messages = channel.history.retrievePast(100)
                            .await()

                        // Sort from the newest message to the oldest message
                        val attachmentUrl = messages.sortedByDescending { it.timeCreated }
                            .flatMap { it.attachments }
                            .firstOrNull {
                                // Only get filenames ending with "image" extensions
                                it.contentType != null && it.contentType in ContentTypeUtils.COMMON_IMAGE_CONTENT_TYPES
                            }?.url

                        if (attachmentUrl != null) {
                            // Found a valid URL, let's go!
                            return attachmentUrl
                        }
                    }
                }
            }

            return null
        }
    }

    suspend fun get(context: UnleashedContext, searchHistory: Boolean = true): String {
        val imageUrl = getImageUrl(context, dataValue, searchHistory)
        if (imageUrl != null)
            return imageUrl

        context.fail(context.wasInitiallyDeferredEphemerally ?: true) {
            styled(
                context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound),
                Emotes.LoriSob
            )
        }
    }
}

class ImageReferenceOrAttachmentDiscordOptionReference<T>(name: String) : OptionReference<T>(name)

class ImageReferenceOrAttachment(
    val dataValue: String?,
    val attachment: Attachment?,
) {
    suspend fun get(context: UnleashedContext, searchHistory: Boolean = true): String {
        // Attachments take priority
        if (attachment != null) {
            val contentType = attachment.contentType
            if (contentType != null && contentType in ContentTypeUtils.COMMON_IMAGE_CONTENT_TYPES)
                return attachment.url

            // This ain't an image dawg! Because the user explicitly provided the image, then let's fail
            context.fail(context.wasInitiallyDeferredEphemerally ?: true) {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound),
                    Emotes.LoriSob
                )
            }
        }

        val imageUrl = getImageUrl(context, dataValue, searchHistory)
        if (imageUrl != null)
            return imageUrl

        context.fail(context.wasInitiallyDeferredEphemerally ?: true) {
            styled(
                context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound),
                Emotes.LoriSob
            )
        }
    }
}