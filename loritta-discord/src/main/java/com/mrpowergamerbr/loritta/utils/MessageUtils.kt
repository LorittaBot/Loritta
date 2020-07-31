package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxEmbed
import mu.KotlinLogging
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.utils.Placeholders

object MessageUtils {
	private val logger = KotlinLogging.logger {}

	fun generateMessage(message: String, sources: List<Any>?, guild: Guild?, customTokens: Map<String, String> = mutableMapOf(), safe: Boolean = true): Message? {
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
				val parallaxEmbed = Loritta.GSON.fromJson<ParallaxEmbed>(jsonObject["embed"])
				messageBuilder.setEmbed(parallaxEmbed.toDiscordEmbed(safe))
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
	}
}

/**
 * When an user adds a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAdd(context: CommandContext, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onReactionAdd = function
	return this
}

/**
 * When an user removes a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionRemove(context: CommandContext, function: suspend (MessageReactionRemoveEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onReactionRemove = function
	return this
}

/**
 * When the command executor adds a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAddByAuthor(context: CommandContext, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onReactionAddByAuthor = function
	return this
}

/**
 * When the command executor adds a reaction to this message
 *
 * @param userId   the user ID
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAddByAuthor(userId: Long, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(null, this.channel.idLong, userId) }
	functions.onReactionAddByAuthor = function
	return this
}

/**
 * When the command executor removes a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionRemoveByAuthor(context: CommandContext, function: suspend (MessageReactionRemoveEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onReactionRemoveByAuthor = function
	return this
}

/**
 * When an user sends a message on the same text channel as the executed command
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onResponse(context: CommandContext, function: suspend (LorittaMessageEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onResponse = function
	return this
}

/**
 * When the command executor sends a message on the same text channel as the executed command
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onResponseByAuthor(context: CommandContext, function: suspend (LorittaMessageEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onResponseByAuthor = function
	return this
}

/**
 * When a message is received in any guild
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onMessageReceived(context: CommandContext, function: suspend (LorittaMessageEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onMessageReceived = function
	return this
}

/**
 * When an user adds a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAdd(context: LorittaCommandContext, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	if (context !is DiscordCommandContext)
		throw UnsupportedOperationException("I don't know how to handle a $context yet!")

	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onReactionAdd = function
	return this
}

/**
 * When an user removes a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionRemove(context: LorittaCommandContext, function: suspend (MessageReactionRemoveEvent) -> Unit): Message {
	if (context !is DiscordCommandContext)
		throw UnsupportedOperationException("I don't know how to handle a $context yet!")

	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onReactionRemove = function
	return this
}

/**
 * When the command executor adds a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAddByAuthor(context: LorittaCommandContext, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	if (context !is DiscordCommandContext)
		throw UnsupportedOperationException("I don't know how to handle a $context yet!")

	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onReactionAddByAuthor = function
	return this
}

/**
 * When the command executor adds a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAddByAuthor(context: net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong
	return onReactionAddByAuthor(context.user.idLong, guildId, channelId, function)
}

/**
 * When the command executor adds a reaction to this message
 *
 * @param userId    the user's ID
 * @param guildId   the guild's ID, may be null
 * @param channelId the channel's ID, may be null
 * @return          the message object for chaining
 */
fun Message.onReactionAddByAuthor(userId: Long, guildId: Long?, channelId: Long?, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, userId) }
	functions.onReactionAddByAuthor = function
	return this
}

/**
 * When the command executor removes a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionRemoveByAuthor(context: LorittaCommandContext, function: suspend (MessageReactionRemoveEvent) -> Unit): Message {
	if (context !is DiscordCommandContext)
		throw UnsupportedOperationException("I don't know how to handle a $context yet!")

	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onReactionRemoveByAuthor = function
	return this
}

/**
 * When the command executor adds or removes a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionByAuthor(context: LorittaCommandContext, function: suspend (GenericMessageReactionEvent) -> Unit): Message {
	if (context !is DiscordCommandContext)
		throw UnsupportedOperationException("I don't know how to handle a $context yet!")

	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	onReactionByAuthor(context.userHandle.idLong, guildId, channelId, function)
	return this
}

/**
 * When the command executor adds or removes a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionByAuthor(context: net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext, function: suspend (GenericMessageReactionEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	onReactionByAuthor(context.user.idLong, guildId, channelId, function)
	return this
}

/**
 * When the command executor adds or removes a reaction to this message
 *
 * @param userId    the user's ID
 * @param guildId   the guild's ID, may be null
 * @param channelId the channel's ID, may be null
 * @param function  the callback that should be invoked
 * @return          the message object for chaining
 */
fun Message.onReactionByAuthor(userId: Long, guildId: Long?, channelId: Long?, function: suspend (GenericMessageReactionEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, userId) }
	functions.onReactionByAuthor = function
	return this
}

/**
 * When an user sends a message on the same text channel as the executed command
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onResponse(context: LorittaCommandContext, function: suspend (LorittaMessageEvent) -> Unit): Message {
	if (context !is DiscordCommandContext)
		throw UnsupportedOperationException("I don't know how to handle a $context yet!")

	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onResponse = function
	return this
}

/**
 * When the command executor sends a message on the same text channel as the executed command
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onResponseByAuthor(context: LorittaCommandContext, function: suspend (LorittaMessageEvent) -> Unit): Message {
	if (context !is DiscordCommandContext)
		throw UnsupportedOperationException("I don't know how to handle a $context yet!")

	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onResponseByAuthor = function
	return this
}

/**
 * Removes all interaction functions associated with [this]
 */
fun Message.removeAllFunctions(): Message {
	loritta.messageInteractionCache.remove(this.idLong)
	return this
}

/**
 * When a message is received in any guild
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onMessageReceived(context: LorittaCommandContext, function: suspend (LorittaMessageEvent) -> Unit): Message {
	if (context !is DiscordCommandContext)
		throw UnsupportedOperationException("I don't know how to handle a $context yet!")

	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onMessageReceived = function
	return this
}

class MessageInteractionFunctions(val guildId: Long?, val channelId: Long?, val originalAuthor: Long) {
	// Caso guild == null, quer dizer que foi uma mensagem recebida via DM!
	var onReactionAdd: (suspend (MessageReactionAddEvent) -> Unit)? = null
	var onReactionRemove: (suspend (MessageReactionRemoveEvent) -> Unit)? = null
	var onReactionAddByAuthor: (suspend (MessageReactionAddEvent) -> Unit)? = null
	var onReactionRemoveByAuthor: (suspend (MessageReactionRemoveEvent) -> Unit)? = null
	var onReactionByAuthor: (suspend (GenericMessageReactionEvent) -> Unit)? = null
	var onResponse: (suspend (LorittaMessageEvent) -> Unit)? = null
	var onResponseByAuthor: (suspend (LorittaMessageEvent) -> Unit)? = null
	var onMessageReceived: (suspend (LorittaMessageEvent) -> Unit)? = null
}