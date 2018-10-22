package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mongodb.client.model.Updates
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta

class QuickPunishmentCommand : AbstractCommand("quickpunishment", category = CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["QUICKPUNISHMENT_Description"]
	}

	override fun getExample(): List<String> {
		return listOf()
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val userData = context.config.getUserData(context.userHandle.id)

		if (userData.quickPunishment) {
			context.reply(
					LoriReply(
							message = locale["QUICKPUNISHMENT_Disabled"]
					)
			)

			loritta.updateLorittaGuildUserData(
					context.config,
					context.userHandle.id,
					Updates.set("guildUserData.$.quickPunishment", false)
			)
		} else {
			context.reply(
					LoriReply(
							message = locale["QUICKPUNISHMENT_Enabled"]
					)
			)

			loritta.updateLorittaGuildUserData(
					context.config,
					context.userHandle.id,
					Updates.set("guildUserData.$.quickPunishment", true)
			)
		}
	}
}