package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import com.mrpowergamerbr.loritta.utils.stripNewLines
import com.mrpowergamerbr.loritta.utils.substringIfNeeded

class AfkCommand : AbstractCommand("afk", listOf("awayfromthekeyboard"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["AFK_Description"];
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var profile = context.lorittaUser.profile

		if (profile.isAfk) {
			profile.isAfk = false
			profile.afkReason = null

			loritta save profile

			context.reply(
					LoriReply(
							message = context.locale["AFK_AfkOff"],
							prefix = "\uD83D\uDC24"
					)
			)
		} else {
			val reason = context.args.joinToString(" ").stripNewLines().stripCodeMarks().substringIfNeeded(range = 0..299)

			if (reason.isNotEmpty()) {
				profile.afkReason = reason
			} else {
				profile.afkReason = null
			}

			profile.isAfk = true

			loritta save profile

			context.reply(
					LoriReply(
							message = context.locale["AFK_AfkOn"],
							prefix = "\uD83D\uDE34"
					)
			)
		}
	}
}