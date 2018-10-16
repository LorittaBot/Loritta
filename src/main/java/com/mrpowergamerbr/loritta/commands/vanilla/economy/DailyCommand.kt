package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class DailyCommand : AbstractCommand("daily", listOf("diário", "bolsafamilia", "bolsafamília"), CommandCategory.ECONOMY) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["DAILY_Description"];
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		// TODO: Fix
		/* val votedAt = context.lorittaUser.profile.receivedDailyAt

		val calendar = Calendar.getInstance()
		calendar.timeInMillis = votedAt
		calendar.set(Calendar.HOUR_OF_DAY, 0)
		calendar.set(Calendar.MINUTE, 0)
		calendar.add(Calendar.DAY_OF_MONTH, 1)
		val tomorrow = calendar.timeInMillis

		val canGetDaily = System.currentTimeMillis() > tomorrow

		if (!canGetDaily) {
			context.reply(
					locale["DAILY_PleaseWait", DateUtils.formatDateDiff(tomorrow, locale)],
					Constants.ERROR
			)
			return
		}

		context.reply(
				LoriReply(
						locale["DAILY_DailyLink", "${Loritta.config.websiteUrl}daily"],
						"\uD83D\uDCB3"
				)
		) */
	}
}