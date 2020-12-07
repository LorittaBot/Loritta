package net.perfectdreams.loritta.commands.vanilla.discord

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.utils.*
import net.dv8tion.jda.api.entities.Emote
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.utils.Emotes

class EmojiCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("emoji"), CommandCategory.DISCORD) {
	companion object {
		private const val LOCALE_PREFIX = "commands.discord.emoji"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		usage {
			argument(ArgumentType.EMOTE) {}
		}

		examples {
			+ "\uD83D\uDE0F"
		}

		needsToUploadFiles = true

		executesDiscord {
			val context = this

			if (context.args.size == 1) {
				val arg0 = context.args[0]
				val firstEmote = context.discordMessage.emotes.firstOrNull()
				if (arg0 == firstEmote?.asMention) {
					// Emoji do Discord (via menção)
					downloadAndSendDiscordEmote(context, firstEmote)
					return@executesDiscord
				}

				if (arg0.isValidSnowflake()) {
					val emote = lorittaShards.getEmoteById(arg0)
					if (emote != null) {
						// Emoji do Discord (via ID)
						downloadAndSendDiscordEmote(context, emote)
						return@executesDiscord
					} else {
						context.reply(
								LorittaReply(
										locale["commands.discord.emoji.notFoundId", "`$arg0`"],
										Constants.ERROR
								)
						)
						return@executesDiscord
					}
				}

				val guild = context.guild
				val foundEmote = guild.getEmotesByName(arg0, true).firstOrNull()
				if (foundEmote != null) {
					// Emoji do Discord (via nome)
					downloadAndSendDiscordEmote(context, foundEmote)
					return@executesDiscord
				}

				val isUnicodeEmoji = Constants.EMOJI_PATTERN.matcher(arg0).find()

				if (isUnicodeEmoji) {
					val value = ImageUtils.getTwitterEmojiUrlId(arg0)
					try {
						if (HttpRequest.get("https://twemoji.maxcdn.com/2/72x72/$value.png").code() != 200) {
							context.reply(
									LorittaReply(
											context.locale["commands.discord.emoji.errorWhileDownloadingEmoji", Emotes.LORI_SHRUG],
											Constants.ERROR
									)
							)
							return@executesDiscord
						}
						val emojiImage = LorittaUtils.downloadImage("https://twemoji.maxcdn.com/2/72x72/$value.png")
						context.sendImage(JVMImage(emojiImage!!), "emoji.png", " ")
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}
			} else {
				context.explain()
			}
		}
	}

	suspend fun downloadAndSendDiscordEmote(context: DiscordCommandContext, emote: Emote) {
		val emojiUrl = emote.imageUrl

		try {
			val emojiImage = LorittaUtils.downloadFile("$emojiUrl?size=2048", 5000)
			var fileName = emote.name
			if (emote.isAnimated) {
				fileName += ".gif"
			} else {
				fileName += ".png"
			}
			context.sendFile(emojiImage!!, fileName, " ")
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}