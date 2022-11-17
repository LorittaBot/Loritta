package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import com.github.kevinsawicki.http.HttpRequest
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot

class EmojiCommand(loritta: LorittaBot) : AbstractCommand(loritta, "emoji", category = net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.emoji.description")

	// TODO: Fix Usage

	override fun getExamples(): List<String> {
		return listOf("😏")
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.size == 1) {
			val arg0 = context.rawArgs[0]
			val firstEmote = context.message.mentions.customEmojis.firstOrNull()
			if (arg0 == firstEmote?.asMention) {
				// Emoji do Discord (via menção)
				downloadAndSendDiscordEmote(context, firstEmote)
				return
			}

			if (arg0.isValidSnowflake()) {
				val emote = loritta.lorittaShards.getEmoteById(arg0)
				if (emote != null) {
					// Emoji do Discord (via ID)
					downloadAndSendDiscordEmote(context, emote)
					return
				} else {
					context.reply(
                            LorittaReply(
                                    locale["commands.command.emoji.notFoundId", "`$arg0`"],
                                    Constants.ERROR
                            )
					)
					return
				}
			}

			val guild = context.guild
			val foundEmote = guild.getEmojisByName(arg0, true).firstOrNull()
			if (foundEmote != null) {
				// Emoji do Discord (via nome)
				downloadAndSendDiscordEmote(context, foundEmote)
				return
			}

			val isUnicodeEmoji = Constants.EMOJI_PATTERN.matcher(arg0).find()

			if (isUnicodeEmoji) {
				val value = ImageUtils.getTwitterEmojiUrlId(arg0)
				try {
					if (HttpRequest.get("https://twemoji.maxcdn.com/2/72x72/$value.png").code() != 200) {
						context.reply(
                                LorittaReply(
                                        context.locale["commands.command.emoji.errorWhileDownloadingEmoji", Emotes.LORI_SHRUG],
                                        Constants.ERROR
                                )
						)
						return
					}
					val emojiImage = LorittaUtils.downloadImage(loritta, "https://twemoji.maxcdn.com/2/72x72/$value.png")
					context.sendFile(emojiImage!!, "emoji.png", MessageCreateBuilder().addContent(" ").build())
				} catch (e: Exception) {
					e.printStackTrace()
				}
			} else {
				context.explain()
			}
		} else {
			context.explain()
		}
	}

	suspend fun downloadAndSendDiscordEmote(context: CommandContext, emote: CustomEmoji) {
		val emojiUrl = emote.imageUrl

		try {
			val emojiImage = LorittaUtils.downloadFile(loritta, "$emojiUrl?size=2048", 5000)
			var fileName = emote.name
			if (emote.isAnimated) {
				fileName += ".gif"
			} else {
				fileName += ".png"
			}
			context.sendFile(emojiImage!!, fileName, MessageCreateBuilder().addContent(" "))
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}