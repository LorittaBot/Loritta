package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes

class DailyCommand : AbstractCommand("daily", listOf("diário", "bolsafamilia", "bolsafamília"), CommandCategory.ECONOMY) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.economy.daily.description"]
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		// 1. Pegue quando o daily foi pego da última vez
		// 2. Pegue o tempo de quando seria amanhã
		// 3. Compare se o tempo atual é maior que o tempo de amanhã
		val (canGetDaily, tomorrow) = context.lorittaUser.profile.canGetDaily()

		if (!canGetDaily) {
			context.reply(
                    LorittaReply(
                            locale.toNewLocale()["commands.economy.daily.pleaseWait", DateUtils.formatDateDiff(tomorrow, locale)],
                            Constants.ERROR
                    ),
                    LorittaReply(
                            locale.toNewLocale()["commands.economy.daily.pleaseWaitBuySonhos", "<${loritta.instanceConfig.loritta.website.url}user/@me/dashboard/bundles>"],
                            "\uD83D\uDCB3"
                    )
			)
			return
		}

		context.reply(
                LorittaReply(
                        locale.toNewLocale()["commands.economy.daily.dailyLink", "${loritta.instanceConfig.loritta.website.url}daily"],
                        Emotes.LORI_RICH
                ),
                LorittaReply(
                        locale.toNewLocale()["commands.economy.daily.dailyLinkBuySonhos", "<${loritta.instanceConfig.loritta.website.url}user/@me/dashboard/bundles>"],
                        "\uD83D\uDCB3"
                )
		)
	}
}