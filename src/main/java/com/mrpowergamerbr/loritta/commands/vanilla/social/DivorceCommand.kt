package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class DivorceCommand : AbstractCommand("divorce", listOf("divorciar"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["DIVORCE_Description"]
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		// TODO: Fix
		/* if (context.lorittaUser.profile.marriedWith != null) {
			loritta.usersColl.updateMany(
					Filters.`in`(
							"_id",
							listOf(context.userHandle.id, context.lorittaUser.profile.marriedWith)
					),
					Updates.unset("marriedWith")
			)

			context.reply(
					LoriReply(
							"Você se divorciou!",
							"\uD83D\uDC94"
					)
			)
		} else {
			context.reply(
					LoriReply(
							"Você não está casado!",
							Constants.ERROR
					)
			)
		} */
	}
}