package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class QuickPunishmentCommand : AbstractCommand("quickpunishment", category = CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.moderation.quickpunishment.description"]
	}

	override fun getExamples(): List<String> {
		return listOf()
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val userData = context.config.getUserData(context.userHandle.idLong)

		if (userData.quickPunishment) {
			context.reply(
                    LorittaReply(
                            message = locale.toNewLocale()["commands.moderation.quickpunishment.disabled"]
                    )
			)
		} else {
			context.reply(
                    LorittaReply(
                            message = locale.toNewLocale()["commands.moderation.quickpunishment.enabled"]
                    )
			)
		}

		loritta.newSuspendedTransaction {
			userData.quickPunishment = !userData.quickPunishment
		}
	}
}