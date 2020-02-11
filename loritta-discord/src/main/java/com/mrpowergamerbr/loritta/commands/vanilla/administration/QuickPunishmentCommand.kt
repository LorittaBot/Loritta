package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.jetbrains.exposed.sql.transactions.transaction

class QuickPunishmentCommand : AbstractCommand("quickpunishment", category = CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["QUICKPUNISHMENT_Description"]
	}

	override fun getExamples(): List<String> {
		return listOf()
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val userData = context.legacyConfig.getUserData(context.userHandle.idLong)

		if (userData.quickPunishment) {
			context.reply(
					LoriReply(
							message = locale.toNewLocale()["commands.moderation.quickpunishment.disabled"]
					)
			)
		} else {
			context.reply(
					LoriReply(
							message = locale.toNewLocale()["commands.moderation.quickpunishment.enabled"]
					)
			)
		}

		transaction(Databases.loritta) {
			userData.quickPunishment = !userData.quickPunishment
		}
	}
}