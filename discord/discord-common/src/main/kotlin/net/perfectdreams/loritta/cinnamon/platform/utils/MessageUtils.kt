package net.perfectdreams.loritta.cinnamon.platform.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.cinnamon.platform.utils.parallax.ParallaxMessage
import net.perfectdreams.loritta.cinnamon.platform.utils.sources.TokenSource

object MessageUtils {
    private val jsonIgnoreUnknownKeys = Json {
        ignoreUnknownKeys = true
    }

    fun createMessage(
        message: String,
        sources: List<TokenSource>,
        tokens: Map<String, String?>
    ): ParallaxMessage {
        // TODO: Proper message validation?

        // Is this a JSON message?
        val rawParallaxMessage = try {
            val element = jsonIgnoreUnknownKeys.parseToJsonElement(message)
                .jsonObject

            val updatedMessageFormat = updateMessageFormat(element)

            jsonIgnoreUnknownKeys.decodeFromJsonElement(updatedMessageFormat)
        } catch (e: Exception) {
            // Nope, doesn't seem like it! Let's create a ParallaxMessage from the message then...
            try {
                ParallaxMessage(message, listOf())
            } catch (e: Exception) {
                // Okay now you are just being annoying, the message couldn't be validated!
                return ParallaxMessage("Invalid Message", listOf())
            }
        }

        val parsedTokens = mutableMapOf<String, String?>()

        for (source in sources)
            parsedTokens.putAll(source.tokens().map { it.key.name to it.value })

        // Custom tokens always overrides the provided sources
        parsedTokens.putAll(tokens)

        return with(rawParallaxMessage) {
            copy(
                content = replaceTokensIfNotNull(content, parsedTokens),
                embeds = embeds.map {
                    with(it) {
                        it.copy(
                            title = replaceTokensIfNotNull(title, parsedTokens),
                            description = replaceTokensIfNotNull(description, parsedTokens),
                            url = replaceTokensIfNotNull(url, parsedTokens),
                            footer = with(footer) {
                                this?.copy(
                                    text = replaceTokens(text, parsedTokens),
                                    iconUrl = replaceTokensIfNotNull(iconUrl, parsedTokens)
                                )
                            },
                            image = with(image) {
                                this?.copy(
                                    url = replaceTokens(url, parsedTokens)
                                )
                            },
                            thumbnail = with(thumbnail) {
                                this?.copy(
                                    url = replaceTokens(url, parsedTokens)
                                )
                            },
                            fields = fields.map {
                                it.copy(
                                    name = replaceTokens(it.name, parsedTokens),
                                    value = replaceTokens(it.value, parsedTokens),
                                    inline = it.inline
                                )
                            }
                        )
                    }
                }
            )
        }
    }

    private fun replaceTokensIfNotNull(text: String?, tokens: Map<String, String?>) = text?.let { replaceTokens(text, tokens) }
    private fun replaceTokens(text: String, tokens: Map<String, String?>): String {
        var message = text

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

    /* fun generateMessage(message: String, sources: List<Any>?, guild: Guild?, customTokens: Map<String, String> = mutableMapOf(), safe: Boolean = true): Message? {
        val jsonObject = try {
            JsonParser.parseString(message).obj
        } catch (ex: Exception) {
            null
        }

        val tokens = mutableMapOf<String, String?>()
        tokens.putAll(customTokens)

        if (sources != null) {
            for (source in sources) {
                if (source is User) {
                    tokens[Placeholders.USER_MENTION.name] = source.asMention
                    tokens[Placeholders.USER_NAME_SHORT.name] = source.name
                    tokens[Placeholders.USER_NAME.name] = source.name
                    tokens[Placeholders.USER_DISCRIMINATOR.name] = source.discriminator
                    tokens[Placeholders.USER_ID.name] = source.id
                    tokens[Placeholders.USER_AVATAR_URL.name] = source.effectiveAvatarUrl
                    tokens[Placeholders.USER_TAG.name] = source.asTag

                    tokens[Placeholders.Deprecated.USER_DISCRIMINATOR.name] = source.discriminator
                    tokens[Placeholders.Deprecated.USER_ID.name] = source.id
                    tokens[Placeholders.Deprecated.USER_AVATAR_URL.name] = source.effectiveAvatarUrl
                }
                if (source is Member) {
                    tokens[Placeholders.USER_MENTION.name] = source.asMention
                    tokens[Placeholders.USER_NAME_SHORT.name] = source.user.name
                    tokens[Placeholders.USER_NAME.name] = source.user.name
                    tokens[Placeholders.USER_DISCRIMINATOR.name] = source.user.discriminator
                    tokens[Placeholders.USER_ID.name] = source.id
                    tokens[Placeholders.USER_TAG.name] = source.user.asTag
                    tokens[Placeholders.USER_AVATAR_URL.name] = source.user.effectiveAvatarUrl
                    tokens[Placeholders.USER_NICKNAME.name] = source.effectiveName

                    tokens[Placeholders.Deprecated.USER_DISCRIMINATOR.name] = source.user.discriminator
                    tokens[Placeholders.Deprecated.USER_ID.name] = source.id
                    tokens[Placeholders.Deprecated.USER_AVATAR_URL.name] = source.user.effectiveAvatarUrl
                    tokens[Placeholders.Deprecated.USER_NICKNAME.name] = source.effectiveName
                }
                if (source is Guild) {
                    val guildSize = source.memberCount.toString()
                    val mentionOwner = source.owner?.asMention ?: "???"
                    val owner = source.owner?.effectiveName ?: "???"
                    tokens["guild"] = source.name
                    tokens["guildsize"] = guildSize
                    tokens["guild-size"] = guildSize
                    tokens["@owner"] = mentionOwner
                    tokens["owner"] = owner
                    tokens["guild-icon-url"] = source.iconUrl?.replace("jpg", "png")
                }
                if (source is GuildChannel) {
                    tokens["channel"] = source.name
                    tokens["channel-id"] = source.id
                }
                if (source is TextChannel) {
                    tokens["@channel"] = source.asMention
                }
            }
        }

        val messageBuilder = MessageBuilder()
        if (jsonObject != null) {
            // alterar tokens
            handleJsonTokenReplacer(jsonObject, sources, guild, tokens)
            val jsonEmbed = jsonObject["embed"].nullObj
            if (jsonEmbed != null) {
                try {
                    val parallaxEmbed = Loritta.GSON.fromJson<ParallaxEmbed>(jsonObject["embed"])
                    messageBuilder.setEmbed(parallaxEmbed.toDiscordEmbed(safe))
                } catch (e: Exception) {
                    // Creating a empty embed can cause errors, so we just wrap it in a try .. catch block and hope
                    // for the best!
                }
            }
            messageBuilder.append(jsonObject.obj["content"].nullString ?: " ")
        } else {
            messageBuilder.append(replaceTokens(message, sources, guild, tokens).substringIfNeeded())
        }
        if (messageBuilder.isEmpty)
            return null
        return messageBuilder.build()
    }

    private fun handleJsonTokenReplacer(jsonObject: JsonObject, sources: List<Any>?, guild: Guild?, customTokens: Map<String, String?> = mutableMapOf()) {
        for ((key, value) in jsonObject.entrySet()) {
            when {
                value.isJsonPrimitive && value.asJsonPrimitive.isString -> {
                    jsonObject[key] = replaceTokens(value.string, sources, guild, customTokens)
                }
                value.isJsonObject -> {
                    handleJsonTokenReplacer(value.obj, sources, guild, customTokens)
                }
                value.isJsonArray -> {
                    val array = JsonArray()
                    for (it in value.array) {
                        if (it.isJsonPrimitive && it.asJsonPrimitive.isString) {
                            array.add(replaceTokens(it.string, sources, guild, customTokens))
                            continue
                        } else if (it.isJsonObject) {
                            handleJsonTokenReplacer(it.obj, sources, guild, customTokens)
                        }
                        array.add(it)
                    }
                    jsonObject[key] = array
                }
            }
        }
    }

    private fun replaceTokens(text: String, sources: List<Any>?, guild: Guild?, customTokens: Map<String, String?> = mutableMapOf()): String {
        var message = text

        for ((token, value) in customTokens)
            message = message.replace("{$token}", value ?: "\uD83E\uDD37")

        // Para evitar pessoas perguntando "porque os emojis não funcionam???", nós iremos dar replace automaticamente em algumas coisas
        // para que elas simplesmente "funcionem:tm:"
        // Ou seja, se no chat do Discord aparece corretamente, é melhor que na própria Loritta também apareça, não é mesmo?
        if (guild != null) {
            for (emote in guild.emotes) {
                var index = 0
                var overflow = 0
                while (message.indexOf(":${emote.name}:", index) != -1) {
                    if (overflow == 999) {
                        logger.warn { "String $message was overflown (999 > $overflow) when processing emotes, breaking current execution"}
                        logger.warn { "Stuck while processing emote $emote, index = $index, indexOf = ${message.indexOf(":${emote.name}:", index)}"}
                        break
                    }
                    val _index = index
                    index = message.indexOf(":${emote.name}:", index) + 1
                    if (message.indexOf(":${emote.name}:", _index) == 0 || (message[message.indexOf(":${emote.name}:", _index) - 1] != 'a' && message[message.indexOf(":${emote.name}:", _index) - 1] != '<')) {
                        message = message.replaceRange(index - 1..(index - 2) + ":${emote.name}:".length, emote.asMention)
                    }
                    overflow++
                }
            }
            for (textChannel in guild.textChannels) {
                message = message.replace("#${textChannel.name}", textChannel.asMention)
            }
            for (roles in guild.roles) {
                message = message.replace("@${roles.name}", roles.asMention)
            }
        }

        return message
    } */
}