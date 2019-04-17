package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Emote
import net.perfectdreams.loritta.api.commands.CommandCategory

class EmojiCommand : AbstractCommand("emoji", category = CommandCategory.DISCORD) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["EMOJI_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "emoji"
	}

	override fun getExamples(): List<String> {
		return listOf("😏")
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.size == 1) {
			val arg0 = context.rawArgs[0]
			val firstEmote = context.message.emotes.firstOrNull()
			if (arg0 == firstEmote?.asMention) {
				// Emoji do Discord (via menção)
				downloadAndSendDiscordEmote(context, firstEmote)
				return
			}

			if (arg0.isValidSnowflake()) {
				val emote = lorittaShards.getEmoteById(arg0)
				if (emote != null) {
					// Emoji do Discord (via ID)
					downloadAndSendDiscordEmote(context, emote)
					return
				} else {
					context.reply(
							LoriReply(
									locale.toNewLocale()["commands.discord.emoji.notFoundId", "`$arg0`"],
									Constants.ERROR
							)
					)
					return
				}
			}

			val guild = context.guild
			val foundEmote = guild.getEmotesByName(arg0, true).firstOrNull()
			if (foundEmote != null) {
				// Emoji do Discord (via nome)
				downloadAndSendDiscordEmote(context, foundEmote)
				return
			}

			val isUnicodeEmoji = Constants.EMOJI_PATTERN.matcher(arg0).find()

			if (isUnicodeEmoji) {
				val codePoints = mutableListOf<String>()
				for (idx in 0 until arg0.length step 2) {
					var codePoint = LorittaUtils.toUnicode(arg0.codePointAt(idx)).substring(2)
					codePoints += codePoint
				}
				// Vamos usar codepoints porque emojis
				val value = codePoints.joinToString(separator = "-")
				try {
					if (HttpRequest.get("https://twemoji.maxcdn.com/2/72x72/$value.png").code() != 200) {
						context.sendMessage(Constants.ERROR + " **|** ${context.getAsMention(true)}${context.legacyLocale.get("EMOJI_ERROR_WHILE_DOWNLOADING")}")
						return
					}
					val emojiImage = LorittaUtils.downloadImage("https://twemoji.maxcdn.com/2/72x72/$value.png")
					context.sendFile(emojiImage!!, "emoji.png", MessageBuilder().append(" ").build())
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		} else {
			context.explain()
		}
	}

	suspend fun downloadAndSendDiscordEmote(context: CommandContext, emote: Emote) {
		val emojiUrl = emote.imageUrl

		try {
			val emojiImage = LorittaUtils.downloadFile("$emojiUrl?size=2048", 5000)
			var fileName = emote.name
			if (emote.isAnimated) {
				fileName += ".gif"
			} else {
				fileName += ".png"
			}
			context.sendFile(emojiImage!!, fileName, MessageBuilder().append(" ").build())
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}