package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.common.entity.DiscordChannel
import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.DiscordRole
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.cinnamon.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.loritta.cinnamon.discord.LorittaDiscordStuff
import net.perfectdreams.loritta.cinnamon.discord.utils.parallax.ParallaxMessage
import net.perfectdreams.loritta.cinnamon.discord.utils.sources.TokenSource
import net.perfectdreams.loritta.cinnamon.emotes.Emotes

object MessageUtils {
    private val CHAT_EMOJI_REGEX = Regex("(?<!<a?):([A-z0-9_]+):")
    // TODO: Proper i18n
    private val INVALID_MESSAGE_CONFIGURED = ParallaxMessage("*Invalid Message Configured* ${Emotes.LoriSob}", listOf())

    suspend fun createMessage(
        stuff: LorittaDiscordStuff,
        guildId: Snowflake,
        message: String,
        sources: List<TokenSource>,
        tokens: Map<String, String?>
    ): ParallaxMessage {
        val (roles, channels, emojis) = stuff.cache.getDiscordEntitiesOfGuild(guildId)
        return createMessage(
            message,
            sources,
            tokens,
            roles,
            channels,
            emojis
        )
    }

    fun createMessage(
        message: String,
        sources: List<TokenSource>,
        tokens: Map<String, String?>,
        roles: List<DiscordRole>,
        channels: List<DiscordChannel>,
        emojis: List<DiscordEmoji>
    ): ParallaxMessage {
        // TODO: Proper message validation?

        // Is this a JSON message?
        val rawParallaxMessage = try {
            val element = JsonIgnoreUnknownKeys.parseToJsonElement(message)
                .jsonObject

            val updatedMessageFormat = updateMessageFormat(element)

            JsonIgnoreUnknownKeys.decodeFromJsonElement(updatedMessageFormat)
        } catch (e: Exception) {
            // Nope, doesn't seem like it! Let's create a ParallaxMessage from the message then...
            try {
                ParallaxMessage(message, listOf())
            } catch (e: Exception) {
                // Okay now you are just being annoying, the message couldn't be validated!
                return INVALID_MESSAGE_CONFIGURED
            }
        }

        if ((rawParallaxMessage.content == null || rawParallaxMessage.content.isBlank()) && rawParallaxMessage.embeds.isEmpty()) {
            // The user configured a message with no content, let's bail out.
            // TODO: More verifications
            return INVALID_MESSAGE_CONFIGURED
        }

        val parsedTokens = mutableMapOf<String, String?>()

        for (source in sources)
            parsedTokens.putAll(source.tokens().map { it.key.name to it.value })

        // Custom tokens always overrides the provided sources
        parsedTokens.putAll(tokens)

        return with(rawParallaxMessage) {
            copy(
                content = replaceTokensIfNotNull(content, parsedTokens, roles, channels, emojis),
                embeds = embeds.map {
                    with(it) {
                        it.copy(
                            title = replaceTokensIfNotNull(title, parsedTokens, roles, channels, emojis),
                            description = replaceTokensIfNotNull(description, parsedTokens, roles, channels, emojis),
                            url = replaceTokensIfNotNull(url, parsedTokens, roles, channels, emojis),
                            footer = with(footer) {
                                this?.copy(
                                    text = replaceTokens(text, parsedTokens, roles, channels, emojis),
                                    iconUrl = replaceTokensIfNotNull(iconUrl, parsedTokens, roles, channels, emojis)
                                )
                            },
                            image = with(image) {
                                this?.copy(
                                    url = replaceTokens(url, parsedTokens, roles, channels, emojis)
                                )
                            },
                            thumbnail = with(thumbnail) {
                                this?.copy(
                                    url = replaceTokens(url, parsedTokens, roles, channels, emojis)
                                )
                            },
                            fields = fields.map {
                                it.copy(
                                    name = replaceTokens(it.name, parsedTokens, roles, channels, emojis),
                                    value = replaceTokens(it.value, parsedTokens, roles, channels, emojis),
                                    inline = it.inline
                                )
                            }
                        )
                    }
                }
            )
        }
    }

    private fun replaceTokensIfNotNull(
        text: String?,
        tokens: Map<String, String?>,
        roles: List<DiscordRole>,
        channels: List<DiscordChannel>,
        emojis: List<DiscordEmoji>
    ) = text?.let { replaceTokens(text, tokens, roles, channels, emojis) }

    private fun replaceTokens(
        text: String,
        tokens: Map<String, String?>,
        roles: List<DiscordRole>,
        channels: List<DiscordChannel>,
        emojis: List<DiscordEmoji>
    ) = replaceLorittaTokens(replaceDiscordEntities(text, roles, channels, emojis), tokens) // First the Discord Entities, then our user tokens

    private fun replaceDiscordEntities(
        text: String,
        roles: List<DiscordRole>,
        channels: List<DiscordChannel>,
        emojis: List<DiscordEmoji>
    ): String {
        var message = text

        for (role in roles.filter { it.name.isNotBlank() }) {
            message = text.replace("@${role.name}", "<@&${role.id}>")
        }

        for (channel in channels) {
            val name = channel.name.value ?: continue
            if (name.isBlank())
                continue
            message = text.replace("#$name", "<#${channel.id}>")
        }

        // Emojis are kinda tricky, we need to match
        // :lori_clown:
        // but not
        // <:lori_clown:950111543574536212>
        // but that's hard, so how can we do this?
        // ...
        // with the power of RegEx of course! :3
        message = message.replace(CHAT_EMOJI_REGEX) {
            val emojiName = it.groupValues[1]
            val guildEmoji = emojis.firstOrNull { it.name == emojiName }
            if (guildEmoji != null) {
                buildString {
                    append("<")
                    if (guildEmoji.animated.discordBoolean)
                        append("a")
                    append(":")
                    append(guildEmoji.name)
                    append(":")
                    append(guildEmoji.id)
                    append(">")
                }
            } else {
                it.value // Emoji wasn't found, so let's keep it as is
            }
        }

        return message
    }

    private fun replaceLorittaTokens(text: String, tokens: Map<String, String?>): String {
        var message = text

        // Replace tokens
        for ((token, value) in tokens)
            message = message.replace("{$token}", value ?: "\uD83E\uDD37")

        return message
    }

    /**
     * Updates the [json] to match the current implementation of [ParallaxMessage].
     */
    private fun updateMessageFormat(json: JsonObject): JsonObject {
        val mutableMap = json.toMutableMap()

        // Convert "embed" (object) to "embeds" (array)
        popAndRunIfExists(mutableMap, "embed") {
            mutableMap["embeds"] = buildJsonArray {
                add(it)
            }
        }

        return JsonObject(mutableMap)
    }

    /**
     * If [key] is present in the [map], the [key] is removed from [map] and [action] is invoked.
     */
    private fun popAndRunIfExists(map: MutableMap<String, JsonElement>, key: String, action: (JsonElement) -> (Unit)) {
        val value = map[key]
        if (value != null) {
            map.remove(key)
            action.invoke(value)
        }
    }
}