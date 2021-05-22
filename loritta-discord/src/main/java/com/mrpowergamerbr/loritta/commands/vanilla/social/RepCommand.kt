package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.AccountUtils
import net.perfectdreams.loritta.utils.Emotes

class RepCommand : AbstractCommand("rep", listOf("reputation", "reputação", "reputacao"), CommandCategory.SOCIAL) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.reputation.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.reputation.examples")

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getUsage() = arguments {
		argument(ArgumentType.USER) {}
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)

		val user = context.getUserAt(0)
		val lastReputationGiven = loritta.newSuspendedTransaction {
			Reputation.find {
				(Reputations.givenById eq context.userHandle.idLong)
			}.sortedByDescending { it.receivedAt }.firstOrNull()
		}

		if (lastReputationGiven != null) {
			val diff = System.currentTimeMillis() - lastReputationGiven.receivedAt

			if (3_600_000 > diff) {
				val fancy = DateUtils.formatDateDiff(lastReputationGiven.receivedAt + 3.6e+6.toLong(), locale)
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["commands.command.reputation.wait", fancy])
				return
			}
		}

		if (user != null) {
			if (user == context.userHandle) {
				context.reply(
                        LorittaReply(
                                message = locale["commands.command.reputation.repSelf"],
                                prefix = Constants.ERROR
                        )
				)
				return
			}

			val dailyReward = AccountUtils.getUserTodayDailyReward(context.lorittaUser.profile)

			if (dailyReward == null) { // Nós apenas queremos permitir que a pessoa aposte na rifa caso já tenha pegado sonhos alguma vez hoje
				context.reply(
						LorittaReply(
								locale["commands.youNeedToGetDailyRewardBeforeDoingThisAction", context.config.commandPrefix],
								Constants.ERROR
						)
				)
				return
			}

			var url = "${loritta.instanceConfig.loritta.website.url}user/${user.id}/rep"
			if (!context.isPrivateChannel)
				url += "?guild=${context.guild.id}&channel=${context.message.channel.id}"

			context.reply(
                    LorittaReply(
                            locale["commands.command.reputation.reputationLink", url],
                            Emotes.LORI_HAPPY
                    )
			)
		} else {
			if (context.args.isEmpty()) {
				this.explain(context)
			} else {
				context.reply(
                        LorittaReply(
                                message = locale["commands.userDoesNotExist", arg0?.stripCodeMarks()],
                                prefix = Constants.ERROR
                        )
				)
			}
		}
	}
}