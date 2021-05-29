package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class RemoveEmojiCommand : AbstractCommand("removeemoji", listOf("deleteemoji", "deletaremoji", "removeremoji", "delemoji"), CommandCategory.MODERATION) {
	// TODO: Fix Usage

	override fun getDescriptionKey() = LocaleKeyData("commands.command.removeemoji.description")

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
                    LorittaReply(
                            locale["commands.command.removeemoji.success", deletedEmotes, if (deletedEmotes == 1) "emoji" else "emojis"],
                            "\uD83D\uDDD1"
                    )
			)
		} else {
			context.explain()
		}
	}
}