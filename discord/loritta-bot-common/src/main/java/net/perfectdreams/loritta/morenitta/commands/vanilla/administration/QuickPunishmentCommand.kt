package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.loritta
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.messages.LorittaReply
import net.perfectdreams.loritta.common.utils.Emotes

class QuickPunishmentCommand : AbstractCommand("quickpunishment", category = net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
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