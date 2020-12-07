package net.perfectdreams.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase

class AddEmojiCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("addemoji", "adicionaremoji"), CommandCategory.DISCORD) {
	companion object {
		private const val LOCALE_PREFIX = "commands.discord.addemoji"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		usage {
			argument(ArgumentType.TEXT) {}
			argument(ArgumentType.IMAGE) {}
		}

		userRequiredPermissions = listOf(Permission.MANAGE_EMOTES)

		botRequiredPermissions = listOf(Permission.MANAGE_EMOTES)

		executesDiscord {
			val context = this

			var imageArgument = 1
			var emoteName: String? = null

			if (context.discordMessage.emotes.isNotEmpty()) {
				imageArgument = 0
				emoteName = context.discordMessage.emotes[0].name
			}

			if (imageArgument > context.args.size) {
				context.explain()
				return@executesDiscord
			}

			if (emoteName == null)
				emoteName = context.args[0]

			val imageUrl = context.imageUrl(imageArgument, 1) ?: explainAndExit()

			try {
				val os = LorittaUtils.downloadFile(imageUrl, 5000)

				if (os != null) {
					os.use { inputStream ->
						val emote = context.guild.createEmote(emoteName, Icon.from(inputStream)).await()
						context.reply(
								LorittaReply(
										context.locale["$LOCALE_PREFIX.success"],
										emote.asMention
								)
						)
					}
				} else {
					throw RuntimeException("Couldn't download image!")
				}
			} catch (e: Exception) {
				if (e is ErrorResponseException) {
					if (e.errorCode == 30008) {
						context.reply(
								LorittaReply(
										context.locale["$LOCALE_PREFIX.limitReached"],
										Constants.ERROR
								)
						)
						return@executesDiscord
					}
					if (e.errorCode == 400) {
						context.reply(
								LorittaReply(
										context.locale["$LOCALE_PREFIX.emoteTooBig", "`256kb`"],
										Constants.ERROR
								)
						)
						return@executesDiscord
					}
				}

				context.reply(
						LorittaReply(
								context.locale["$LOCALE_PREFIX.error"],
								Constants.ERROR
						)
				)
			}
		}
	}
}