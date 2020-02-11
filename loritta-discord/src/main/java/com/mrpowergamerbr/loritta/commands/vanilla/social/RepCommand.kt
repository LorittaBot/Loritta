package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes
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

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)

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
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.legacyLocale["REP_WAIT", fancy])
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

			var url = "${loritta.instanceConfig.loritta.website.url}user/${user.id}/rep"
			if (!context.isPrivateChannel)
				url += "?guild=${context.guild.id}&channel=${context.message.channel.id}"

			context.reply(
					LoriReply(
							locale.toNewLocale()["commands.social.reputation.reputationLink", url],
							Emotes.LORI_HAPPY
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