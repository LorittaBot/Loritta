package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.Reputation
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.interactions.vanilla.social.RepCommand.Companion.I18N_PREFIX

class RepCommand(loritta: LorittaBot) : AbstractCommand(loritta, "rep", listOf("reputation", "reputação", "reputacao"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
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
				val fancy = TimeFormat.RELATIVE.format(lastReputationGiven.receivedAt + 3.6e+6.toLong())
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

			val dailyReward = AccountUtils.getUserTodayDailyReward(loritta, context.lorittaUser.profile)

			if (dailyReward == null) { // Nós apenas queremos permitir que a pessoa aposte na rifa caso já tenha pegado sonhos alguma vez hoje
				context.reply(
						LorittaReply(
								locale["commands.youNeedToGetDailyRewardBeforeDoingThisAction", context.config.commandPrefix],
								Constants.ERROR
						)
				)
				return
			}

			val reputationsEnabled = loritta.transaction {
				Profile.findById(user.idLong)?.settings?.reputationsEnabled ?: true
			}

			if (!reputationsEnabled) {
				context.reply(
					LorittaReply(
						context.i18nContext.get(I18N_PREFIX.Give.UserHasDisabledReputations(user.asMention)),
						Constants.ERROR
					)
				)
				return
			}

			var url = "${loritta.config.loritta.website.url}user/${user.id}/rep"
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