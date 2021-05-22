package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.Emotes

class QuickPunishmentCommand : AbstractCommand("quickpunishment", category = CommandCategory.MODERATION) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.quickpunishment.description")

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val userData = context.config.getUserData(context.userHandle.idLong)

		if (userData.quickPunishment) {
			context.reply(
                    LorittaReply(
                            message = locale["commands.command.quickpunishment.disabled"]
                    ),
					LorittaReply(
						message = locale["commands.command.quickpunishment.howEnable"],
						prefix = Emotes.LORI_BAN_HAMMER,
						mentionUser = false
					)
			)
		} else {
			context.reply(
                    LorittaReply(
                            message = locale["commands.command.quickpunishment.enabled"]
                    ),
					LorittaReply(
						message = locale["commands.command.quickpunishment.howDisable"],
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