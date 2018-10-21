package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Daily
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DailyCommand : AbstractCommand("daily", listOf("diário", "bolsafamilia", "bolsafamília"), CommandCategory.ECONOMY) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["DAILY_Description"];
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val calendar = Calendar.getInstance()
		calendar.timeInMillis = System.currentTimeMillis()
		calendar.set(Calendar.HOUR_OF_DAY, 0)
		calendar.set(Calendar.MINUTE, 0)
		calendar.add(Calendar.DAY_OF_MONTH, 1)
		val tomorrow = calendar.timeInMillis

		val currentDaily = transaction(Databases.loritta) { Daily.find { (Dailies.receivedById eq context.userHandle.idLong) and (Dailies.receivedAt less tomorrow) }.firstOrNull() }

		if (currentDaily != null) {
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
		)
	}
}