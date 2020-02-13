package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandCategory

class DailyCommand : AbstractCommand("daily", listOf("diário", "bolsafamilia", "bolsafamília"), CommandCategory.ECONOMY) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["DAILY_Description"]
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		// 1. Pegue quando o daily foi pego da última vez
		// 2. Pegue o tempo de quando seria amanhã
		// 3. Compare se o tempo atual é maior que o tempo de amanhã
		val (canGetDaily, tomorrow) = context.lorittaUser.profile.canGetDaily()

		if (!canGetDaily) {
			context.reply(
					locale["DAILY_PleaseWait", DateUtils.formatDateDiff(tomorrow, locale)],
					Constants.ERROR
			)
			return
		}

		context.reply(
				LoriReply(
						locale["DAILY_DailyLink", "${loritta.instanceConfig.loritta.website.url}daily"],
						"\uD83D\uDCB3"
				)
		)
	}
}