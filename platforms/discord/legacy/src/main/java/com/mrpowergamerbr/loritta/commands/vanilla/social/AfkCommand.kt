package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import com.mrpowergamerbr.loritta.utils.stripNewLines
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class AfkCommand : AbstractCommand("afk", listOf("awayfromthekeyboard"), CommandCategory.SOCIAL) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.afk.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.afk.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
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