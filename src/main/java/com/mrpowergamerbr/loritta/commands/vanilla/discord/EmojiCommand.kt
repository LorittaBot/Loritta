package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.MessageBuilder

class EmojiCommand : AbstractCommand("emoji", category = CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["EMOJI_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "emoji"
	}

	override fun getExample(): List<String> {
		return listOf("😏")
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.size == 1) {
			var emoji = context.args[0]

			if (emoji.startsWith(":") && emoji.endsWith(":")) { // Emoji customizado?
				// Sim!
				val customEmotes = context.message.emotes
				if (!customEmotes.isEmpty()) {
					val emote = customEmotes[0]
					val emojiUrl = emote.imageUrl

					try {
						val emojiImage = LorittaUtils.downloadFile(emojiUrl, 5000)
						var fileName = emote.name
						if (emote.isAnimated) {
							fileName += ".gif"
						} else {
							fileName += ".png"
						}
						context.sendFile(emojiImage, fileName, MessageBuilder().append(" ").build())
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}
			} else {
				// Na verdade é um emoji padrão...
				val codePoints = mutableListOf<String>()
				for (idx in 0 until emoji.length step 2) {
					var codePoint = LorittaUtils.toUnicode(emoji.codePointAt(idx)).substring(2)
					codePoints += codePoint
				}
				// Vamos usar codepoints porque emojis
				val value = codePoints.joinToString(separator = "-")
				try {
					if (HttpRequest.get("https://twemoji.maxcdn.com/2/72x72/$value.png").code() != 200) {
						context.sendMessage(Constants.ERROR + " **|** ${context.getAsMention(true)}${context.locale.get("EMOJI_ERROR_WHILE_DOWNLOADING")}")
						return
					}
					val emojiImage = LorittaUtils.downloadImage("https://twemoji.maxcdn.com/2/72x72/$value.png")
					context.sendFile(emojiImage, "emoji.png", MessageBuilder().append(" ").build())
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		} else {
			context.explain()
		}
	}
}