package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxEmbed
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent

object MessageUtils {
	fun generateMessage(message: String, sources: List<Any>?, guild: Guild?, customTokens: Map<String, String> = mutableMapOf<String, String>(), safe: Boolean = true): Message? {
		val jsonObject = try {
			jsonParser.parse(message).obj
		} catch (ex: Exception) {
			null
		}

		val messageBuilder = MessageBuilder()
		if (jsonObject != null) {
			// alterar tokens
			handleJsonTokenReplacer(jsonObject, sources, guild, customTokens)
			val jsonEmbed = jsonObject["embed"].nullObj
			if (jsonEmbed != null) {
				val parallaxEmbed = Loritta.GSON.fromJson<ParallaxEmbed>(jsonObject["embed"])
				messageBuilder.setEmbed(parallaxEmbed.toDiscordEmbed(safe))
			}
			messageBuilder.append(jsonObject.obj["content"].nullString ?: " ")
		} else {
			messageBuilder.append(replaceTokens(message, sources, guild, customTokens).substringIfNeeded())
		}
		if (messageBuilder.isEmpty)
			return null
		return messageBuilder.build()
	}

	fun handleJsonTokenReplacer(jsonObject: JsonObject, sources: List<Any>?, guild: Guild?, customTokens: Map<String, String> = mutableMapOf<String, String>()) {
		for ((key, value) in jsonObject.entrySet()) {
			if (value.isJsonObject) {
				handleJsonTokenReplacer(value.obj, sources, guild, customTokens)
			}
			if (value.isJsonArray) {
				val array = JsonArray()
				for (it in value.array) {
					if (it.isJsonPrimitive) {
						if (it.asJsonPrimitive.isString) {
							array.add(replaceTokens(it.string, sources, guild, customTokens))
							continue
						}
					}
					if (it.isJsonObject) {
						handleJsonTokenReplacer(it.obj, sources, guild, customTokens)
					}
					array.add(it)
				}
				jsonObject[key] = array
			}
			if (value.isJsonPrimitive) {
				if (value.asJsonPrimitive.isString) {
					jsonObject[key] = replaceTokens(value.string, sources, guild, customTokens)
				}
			}
		}
	}

	fun replaceTokens(text: String, sources: List<Any>?, guild: Guild?, customTokens: Map<String, String?> = mutableMapOf<String, String?>()): String {
		var mentionUser = ""
		var user = ""
		var userDiscriminator = ""
		var userId = ""
		var nickname = ""
		var avatarUrl = ""
		var guildName = ""
		var guildSize = ""
		var mentionOwner = ""
		var owner = ""

		if (sources != null) {
			for (source in sources) {
				if (source is User) {
					mentionUser = source.asMention
					user = source.name
					userDiscriminator = source.discriminator
					userId = source.id
					user = source.name
					avatarUrl = source.effectiveAvatarUrl
				}
				if (source is Member) {
					mentionUser = source.asMention
					user = source.user.name
					userDiscriminator = source.user.discriminator
					userId = source.user.id
					nickname = source.effectiveName
					avatarUrl = source.user.effectiveAvatarUrl
				}
				if (source is Guild) {
					guildName = source.name
					guildSize = source.members.size.toString()
					mentionOwner = source.owner.asMention
					owner = source.owner.effectiveName
				}
			}
		}

		var message = text

		for ((token, value) in customTokens) {
			message = message.replace("{$token}", value ?: "\uD83E\uDD37")
		}

		message = message.replace("{@user}", mentionUser)
		message = message.replace("{user}", user.escapeMentions())
		message = message.replace("{user-id}", userId)
		message = message.replace("{user-avatar-url}", avatarUrl)
		message = message.replace("{user-discriminator}", userDiscriminator)
		message = message.replace("{nickname}", nickname.escapeMentions())
		message = message.replace("{guild}", guildName.escapeMentions())
		message = message.replace("{guildsize}", guildSize)
		message = message.replace("{@owner}", mentionOwner)
		message = message.replace("{owner}", owner.escapeMentions())

		// Para evitar pessoas perguntando "porque os emojis não funcionam???", nós iremos dar replace automaticamente em algumas coisas
		// para que elas simplesmente "funcionem:tm:"
		// Ou seja, se no chat do Discord aparece corretamente, é melhor que na própria Loritta também apareça, não é mesmo?
		if (guild != null) {
			for (emote in guild.emotes) {
				var index = 0
				var overflow = 0
				while (message.indexOf(":${emote.name}:", index) != -1) {
					if (overflow == 999) {
						println("Overflow on String ${message}!!!")
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
			for (member in guild.members) {
				message = message.replace("@${member.user.name}#${member.user.discriminator}", member.asMention)
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
fun Message.onReactionAdd(context: CommandContext, function: (MessageReactionAddEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild?.id, context.userHandle.id) }
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
fun Message.onReactionRemove(context: CommandContext, function: (MessageReactionRemoveEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild?.id, context.userHandle.id) }
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
fun Message.onReactionAddByAuthor(context: CommandContext, function: (MessageReactionAddEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild?.id, context.userHandle.id) }
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
fun Message.onReactionAddByAuthor(userId: String, function: (MessageReactionAddEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(null, userId) }
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
fun Message.onReactionRemoveByAuthor(context: CommandContext, function: (MessageReactionRemoveEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild?.id, context.userHandle.id) }
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
fun Message.onResponse(context: CommandContext, function: (LorittaMessageEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild?.id, context.userHandle.id) }
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
fun Message.onResponseByAuthor(context: CommandContext, function: (LorittaMessageEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild?.id, context.userHandle.id) }
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
fun Message.onMessageReceived(context: CommandContext, function: (LorittaMessageEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild?.id, context.userHandle.id) }
	functions.onMessageReceived = function
	return this
}

class MessageInteractionFunctions(val guild: String?, val originalAuthor: String) {
	// Caso guild == null, quer dizer que foi uma mensagem recebida via DM!
	var onReactionAdd: ((MessageReactionAddEvent) -> Unit)? = null
	var onReactionRemove: ((MessageReactionRemoveEvent) -> Unit)? = null
	var onReactionAddByAuthor: ((MessageReactionAddEvent) -> Unit)? = null
	var onReactionRemoveByAuthor: ((MessageReactionRemoveEvent) -> Unit)? = null
	var onResponse: ((LorittaMessageEvent) -> Unit)? = null
	var onResponseByAuthor: ((LorittaMessageEvent) -> Unit)? = null
	var onMessageReceived: ((LorittaMessageEvent) -> Unit)? = null
}