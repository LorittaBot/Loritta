package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.Emotes

class QuickPunishmentCommand : AbstractCommand("quickpunishment", category = CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["commands.moderation.quickpunishment.description"]
	}

	override fun getExamples(): List<String> {
		return listOf()
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val userData = context.config.getUserData(context.userHandle.idLong)

		if (userData.quickPunishment) {
			context.reply(
                    LorittaReply(
                            message = locale["commands.moderation.quickpunishment.disabled"]
                    ),
					LorittaReply(
						message = locale["commands.moderation.quickpunishment.howEnable"],
						prefix = Emotes.LORI_BAN_HAMMER,
						mentionUser = false
					)
			)
		} else {
			context.reply(
                    LorittaReply(
                            message = locale["commands.moderation.quickpunishment.enabled"]
                    ),
					LorittaReply(
						message = locale["commands.moderation.quickpunishment.howDisable"],
						prefix = Emotes.LORI_BAN_HAMMER,
						mentionUser = false
					)
			)
		}

		loritta.newSuspendedTransaction {
			userData.quickPunishment = !userData.quickPunishment
		}
	}
}
