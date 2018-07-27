package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta

class DivorceCommand : AbstractCommand("divorce", listOf("divorciar"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["DIVORCE_Description"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.lorittaUser.profile.marriedWith != null) {
			loritta.usersColl.updateMany(
					Filters.`in`(
							"_id",
							listOf(context.userHandle.id, context.lorittaUser.profile.marriedWith)
					),
					Updates.unset("marriedWith")
			)

			context.reply(
					LoriReply(
							"Você se divorciou!"
					)
			)
		} else {
			context.reply(
					LoriReply(
							"Você não está casado!"
					)
			)
		}
	}
}