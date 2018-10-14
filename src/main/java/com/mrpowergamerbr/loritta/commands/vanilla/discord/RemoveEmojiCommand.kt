package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission

class RemoveEmojiCommand : AbstractCommand("removeemoji", listOf("deleteemoji", "deletaremoji", "removeremoji"), CommandCategory.DISCORD) {
	override fun getUsage(): String {
		return ":emoji1: :emoji2:"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["REMOVEEMOJI_Description"]
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_EMOTES)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_EMOTES)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var deletedEmotes = 0

		context.message.emotes.forEach {
			if (it.guild == context.guild) {
				it.delete().queue()
				deletedEmotes++
			}
		}

		if (deletedEmotes != 0) {
			context.reply(
							LoriReply(
									locale["REMOVEEMOJI_Success", deletedEmotes, if (deletedEmotes == 1) "emoji" else "emojis"],
									"\uD83D\uDDD1"
							)
			)
		} else {
			context.reply(
					LoriReply(
							locale["REMOVEEMOJI_Error"],
							Constants.ERROR
					)
			)
		}
	}
}
