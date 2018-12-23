package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class DivorceCommand : AbstractCommand("divorce", listOf("divorciar"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["DIVORCE_Description"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val marriage = transaction(Databases.loritta) { context.lorittaUser.profile.marriage }

		if (marriage != null) {
			// depois
			transaction(Databases.loritta) {
				Profiles.update({ Profiles.marriage eq marriage.id }) {
					it[Profiles.marriage] = null
				}
				marriage.delete()
			}

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
		}
	}
}