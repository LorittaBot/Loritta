package net.perfectdreams.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase

class RemoveEmojiCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("removeemoji", "deleteemoji", "deletaremoji", "removeremoji"), CommandCategory.DISCORD) {
	companion object {
		private const val LOCALE_PREFIX = "commands.discord.removeemoji"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		usage {
			argument(ArgumentType.EMOTE) {}
			argument(ArgumentType.EMOTE) {}
		}

		userRequiredPermissions = listOf(Permission.MANAGE_EMOTES)

		botRequiredPermissions = listOf(Permission.MANAGE_EMOTES)

		executesDiscord {
			val context = this

			var deletedEmotes = 0

			context.discordMessage.emotes.forEach {
				if (it.guild == context.guild) {
					it.delete().queue()
					deletedEmotes++
				}
			}

			if (deletedEmotes != 0) {
				context.reply(
						LorittaReply(
								locale["$LOCALE_PREFIX.success", deletedEmotes, if (deletedEmotes == 1) "emoji" else "emojis"],
								"\uD83D\uDDD1"
						)
				)
			} else {
				context.reply(
						LorittaReply(
								locale["$LOCALE_PREFIX.noEmojiRemoved"],
								Constants.ERROR
						)
				)
			}
		}
	}
}