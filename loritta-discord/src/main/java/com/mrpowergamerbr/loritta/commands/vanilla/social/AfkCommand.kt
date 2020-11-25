package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class AfkCommand : AbstractCommand("afk", listOf("awayfromthekeyboard"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.social.afk.description"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var profile = context.lorittaUser.profile

		if (profile.isAfk) {
			loritta.newSuspendedTransaction {
				profile.isAfk = false
				profile.afkReason = null
			}

			context.reply(
                    LorittaReply(
                            message = context.locale["commands.social.afk.afkOff"],
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
                            message = context.locale["commands.social.afk.afkOn"],
                            prefix = "\uD83D\uDE34"
                    )
			)
		}
	}
}