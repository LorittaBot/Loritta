package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxEmbed
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent
import java.util.regex.Pattern

object MessageUtils {
	fun generateMessage(message: String, source: Any?, guild: Guild?, customTokens: Map<String, String> = mutableMapOf<String, String>()): Message? {
		val jsonObject = try {
			JSON_PARSER.parse(message).obj
		} catch (ex: Exception) {
			null
		}

		val messageBuilder = MessageBuilder()
		if (jsonObject != null) {
			// alterar tokens
			handleJsonTokenReplacer(jsonObject, source, guild, customTokens)
			val parallaxEmbed = Loritta.GSON.fromJson<ParallaxEmbed>(jsonObject["embed"])
			messageBuilder.setEmbed(parallaxEmbed.toDiscordEmbed())
			messageBuilder.append(jsonObject.obj["content"].nullString ?: " ")
		} else {
			messageBuilder.append(replaceTokens(message, source, guild, customTokens).substringIfNeeded())
		}
		if (messageBuilder.isEmpty)
			return null
		return messageBuilder.build()
	}

	fun handleJsonTokenReplacer(jsonObject: JsonObject, source: Any?, guild: Guild?, customTokens: Map<String, String> = mutableMapOf<String, String>()) {
		for ((key, value) in jsonObject.entrySet()) {
			if (value.isJsonObject) {
				handleJsonTokenReplacer(value.obj, source, guild, customTokens)
			}
			if (value.isJsonArray) {
				val array = JsonArray()
				for (it in value.array) {
					if (it.isJsonPrimitive) {
						if (it.asJsonPrimitive.isString) {
							array.add(replaceTokens(it.string, source, guild, customTokens))
							continue
						}
					}
					if (it.isJsonObject) {
						handleJsonTokenReplacer(it.obj, source, guild, customTokens)
					}
					array.add(it)
				}
				jsonObject[key] = array
			}
			if (value.isJsonPrimitive) {
				if (value.asJsonPrimitive.isString) {
					jsonObject[key] = replaceTokens(value.string, source, guild, customTokens)
				}
			}
		}
	}

	fun replaceTokens(text: String, source: Any?, guild: Guild?, customTokens: Map<String, String?> = mutableMapOf<String, String?>()): String {
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

		if (source is GenericGuildMemberEvent) {
			mentionUser = source.member.asMention
			user = source.member.user.name
			userDiscriminator = source.member.user.discriminator
			userId = source.member.user.id
			avatarUrl = source.member.user.effectiveAvatarUrl
			nickname = source.member.effectiveName
			guildName = source.guild.name
			guildSize = source.guild.members.size.toString()
			mentionOwner = source.guild.owner.asMention
			owner = source.guild.owner.effectiveName
		}

		if (source is MessageReceivedEvent) {
			mentionUser = source.member.asMention
			user = source.member.user.name
			userDiscriminator = source.member.user.discriminator
			userId = source.member.user.id
			avatarUrl = source.member.user.effectiveAvatarUrl
			nickname = source.member.effectiveName
			guildName = source.guild.name
			guildSize = source.guild.members.size.toString()
			mentionOwner = source.guild.owner.asMention
			owner = source.guild.owner.effectiveName
		}

		var message = text

		for ((token, value) in customTokens) {
			message = message.replace("{$token}", value ?: "\uD83E\uDD37")
		}

		message = message.replace("{@user}", mentionUser)
		message = message.replace("{user}", user.escapeMentions())
		message = message.replace("{user-id}", userId)
		message = message.replace("{userAvatarUrl}", avatarUrl)
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

fun Message.onReactionAdd(context: CommandContext, function: (MessageReactionAddEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onReactionAdd = function
	return this
}

fun Message.onReactionRemove(context: CommandContext, function: (MessageReactionRemoveEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onReactionRemove = function
	return this
}

fun Message.onReactionAddByAuthor(context: CommandContext, function: (MessageReactionAddEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onReactionAddByAuthor = function
	return this
}

fun Message.onReactionRemoveByAuthor(context: CommandContext, function: (MessageReactionRemoveEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onReactionRemoveByAuthor = function
	return this
}

fun Message.onResponse(context: CommandContext, function: (MessageReceivedEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onResponse = function
	return this
}

fun Message.onResponseByAuthor(context: CommandContext, function: (MessageReceivedEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onResponseByAuthor = function
	return this
}

fun Message.onMessageReceived(context: CommandContext, function: (MessageReceivedEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.id) { MessageInteractionFunctions(this.guild.id, context.userHandle.id) }
	functions.onMessageReceived = function
	return this
}

class MessageInteractionFunctions(val guild: String, val originalAuthor: String) {
	var onReactionAdd: ((MessageReactionAddEvent) -> Unit)? = null
	var onReactionRemove: ((MessageReactionRemoveEvent) -> Unit)? = null
	var onReactionAddByAuthor: ((MessageReactionAddEvent) -> Unit)? = null
	var onReactionRemoveByAuthor: ((MessageReactionRemoveEvent) -> Unit)? = null
	var onResponse: ((MessageReceivedEvent) -> Unit)? = null
	var onResponseByAuthor: ((MessageReceivedEvent) -> Unit)? = null
	var onMessageReceived: ((MessageReceivedEvent) -> Unit)? = null
}