package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import com.mrpowergamerbr.loritta.utils.stripNewLines
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import org.jetbrains.exposed.sql.transactions.transaction

class AfkCommand : AbstractCommand("afk", listOf("awayfromthekeyboard"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["AFK_Description"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var profile = context.lorittaUser.profile

		if (profile.isAfk) {
			transaction(Databases.loritta) {
				profile.isAfk = false
				profile.afkReason = null
			}

			context.reply(
					LoriReply(
							message = context.legacyLocale["AFK_AfkOff"],
							prefix = "\uD83D\uDC24"
					)
			)
		} else {
			val reason = context.args.joinToString(" ").stripNewLines().stripCodeMarks().substringIfNeeded(range = 0..299)

			transaction(Databases.loritta) {
				if (reason.isNotEmpty()) {
					profile.afkReason = reason
				} else {
					profile.afkReason = null
				}

				profile.isAfk = true
			}

			context.reply(
					LoriReply(
							message = context.legacyLocale["AFK_AfkOn"],
							prefix = "\uD83D\uDE34"
					)
			)
		}
	}
}