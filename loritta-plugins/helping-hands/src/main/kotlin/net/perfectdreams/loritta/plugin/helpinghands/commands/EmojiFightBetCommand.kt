package net.perfectdreams.loritta.plugin.helpinghands.commands

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.plugin.helpinghands.utils.EmojiFight
import net.perfectdreams.loritta.utils.AccountUtils
import net.perfectdreams.loritta.utils.GenericReplies
import net.perfectdreams.loritta.utils.NumberUtils

class EmojiFightBetCommand(val plugin: HelpingHandsPlugin) : DiscordAbstractCommandBase(
		plugin.loritta,
		listOf("emojifight bet", "rinhadeemoji bet", "emotefight bet"),
		CommandCategory.ECONOMY
) {
	override fun command() = create {
		localizedDescription("commands.command.emojifightbet.description")
		localizedExamples("commands.command.emojifightbet.examples")

		usage {
			arguments {
				argument(ArgumentType.NUMBER) {}
				argument(ArgumentType.NUMBER) {
					optional = true
				}
			}
		}

		this.similarCommands = listOf("EmojiFightCommand")
		this.canUseInPrivateChannel = false

		executesDiscord {
			// Gets the first argument
			// If the argument is null (we just show the command explanation and exit)
			// If it is not null, we convert it to a Long (if it is a invalid number, it will be null)
			// Then, in the ".also" block, we check if it is null and, if it is, we show that the user provided a invalid number!
			val totalEarnings = (args.getOrNull(0) ?: explainAndExit())
					.let { NumberUtils.convertShortenedNumberToLong(it) }
					.let {
						if (it == null)
							GenericReplies.invalidNumber(this, args[0])
						it
					}

			if (0 >= totalEarnings)
				fail(locale["commands.command.flipcoinbet.zeroMoney"], Constants.ERROR)

			val selfUserProfile = lorittaUser.profile

			if (totalEarnings > selfUserProfile.money)
				fail(locale["commands.command.flipcoinbet.notEnoughMoneySelf"], Constants.ERROR)

			// Only allow users to participate in a emoji fight bet if the user got their daily reward today
			AccountUtils.getUserTodayDailyReward(lorittaUser.profile)
					?: fail(locale["commands.youNeedToGetDailyRewardBeforeDoingThisAction", serverConfig.commandPrefix], Constants.ERROR)

			val maxPlayersInEvent = (
					(this.args.getOrNull(1) ?.toIntOrNull() ?: EmojiFight.DEFAULT_MAX_PLAYER_COUNT)
							.coerceIn(2, EmojiFight.DEFAULT_MAX_PLAYER_COUNT)
					)

			val emojiFight = EmojiFight(
					plugin,
					this,
					totalEarnings,
					maxPlayersInEvent
			)

			emojiFight.start()
		}
	}
}