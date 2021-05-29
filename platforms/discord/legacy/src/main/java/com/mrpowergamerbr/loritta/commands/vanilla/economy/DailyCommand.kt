package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.utils.Emotes

class DailyCommand : AbstractCommand("daily", listOf("diário", "bolsafamilia", "bolsafamília"), CommandCategory.ECONOMY) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.daily.description")

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		// 1. Pegue quando o daily foi pego da última vez
		// 2. Pegue o tempo de quando seria amanhã
		// 3. Compare se o tempo atual é maior que o tempo de amanhã
		val (canGetDaily, tomorrow) = context.lorittaUser.profile.canGetDaily()

		if (!canGetDaily) {
			context.reply(
				LorittaReply(
					locale["commands.command.daily.pleaseWait", DateUtils.formatDateDiff(tomorrow, locale)],
					Constants.ERROR
				),
				LorittaReply(
					locale["commands.command.daily.pleaseWaitBuySonhos", "<${loritta.instanceConfig.loritta.website.url}user/@me/dashboard/bundles>"],
					"\uD83D\uDCB3"
				)
			)
			return
		}

		val url = if (context.isPrivateChannel)
			"${loritta.instanceConfig.loritta.website.url}daily"
		else // Used for daily multiplier priority
			"${loritta.instanceConfig.loritta.website.url}daily?guild=${context.guild.idLong}"

		context.reply(
			LorittaReply(
				locale["commands.command.daily.dailyLink", url],
				Emotes.LORI_RICH
			),
			LorittaReply(
				context.locale["commands.command.daily.dailyWarning", "${loritta.instanceConfig.loritta.website.url}guidelines"],
				Emotes.LORI_BAN_HAMMER,
				mentionUser = false
			),
			LorittaReply(
				locale["commands.command.daily.dailyLinkBuySonhos", "<${loritta.instanceConfig.loritta.website.url}user/@me/dashboard/bundles>"],
				"\uD83D\uDCB3",
				mentionUser = false
			)
		)
	}
}