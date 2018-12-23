package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import org.jetbrains.exposed.sql.transactions.transaction

class RepCommand : AbstractCommand("rep", listOf("reputation", "reputação", "reputacao"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["REP_DESCRIPTON"]
	}

	override fun getExamples(): List<String> {
		return listOf("@Loritta", "@MrPowerGamerBR")
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val user = context.getUserAt(0)
		val lastReputationGiven = transaction(Databases.loritta) {
			Reputation.find {
				(Reputations.givenById eq context.userHandle.idLong)
			}.sortedByDescending { it.receivedAt }.firstOrNull()
		}

		if (lastReputationGiven != null) {
			val diff = System.currentTimeMillis() - lastReputationGiven.receivedAt

			if (3_600_000 > diff) {
				val fancy = DateUtils.formatDateDiff(lastReputationGiven.receivedAt + 3.6e+6.toLong(), locale)
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["REP_WAIT", fancy])
				return
			}
		}

		if (user != null) {
			if (user == context.userHandle) {
				context.reply(
						LoriReply(
								message = locale["REP_SELF"],
								prefix = Constants.ERROR
						)
				)
				return
			}

			var url = "${Loritta.config.websiteUrl}user/${user.id}/rep"
			if (!context.isPrivateChannel)
				url += "?channel=${context.message.channel.id}"

			context.reply(
					LoriReply(
							url
					)
			)
		} else {
			if (context.args.isEmpty()) {
				this.explain(context)
			} else {
				context.reply(
						LoriReply(
								message = locale["REP_InvalidUser"],
								prefix = Constants.ERROR
						)
				)
			}
		}
	}
}