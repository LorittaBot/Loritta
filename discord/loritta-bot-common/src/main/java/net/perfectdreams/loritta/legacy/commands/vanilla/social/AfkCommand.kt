package net.perfectdreams.loritta.legacy.commands.vanilla.social

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.loritta
import net.perfectdreams.loritta.legacy.utils.stripCodeMarks
import net.perfectdreams.loritta.legacy.utils.stripNewLines
import net.perfectdreams.loritta.legacy.utils.substringIfNeeded
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class AfkCommand : AbstractCommand("afk", listOf("awayfromthekeyboard"), CommandCategory.SOCIAL) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.afk.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.afk.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "afk on")

		var profile = context.lorittaUser.profile

		if (profile.isAfk) {
			loritta.newSuspendedTransaction {
				profile.isAfk = false
				profile.afkReason = null
			}

			context.reply(
                    LorittaReply(
                            message = context.locale["commands.command.afk.afkOff"],
                            prefix = "\uD83D\uDC24"
                    )
			)
		} else {
			val reason = context.args.joinToString(" ").stripNewLines().stripCodeMarks().substringIfNeeded(range = 0..299)

			loritta.newSuspendedTransaction {
				if (reason.isNotEmpty()) {
					profile.afkReason = reason
				} else {
					profile.afkReason = null
				}

				profile.isAfk = true
			}

			context.reply(
                    LorittaReply(
                            message = context.locale["commands.command.afk.afkOn"],
                            prefix = "\uD83D\uDE34"
                    )
			)
		}
	}
}