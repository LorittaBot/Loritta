package net.perfectdreams.loritta.plugin.helpinghands.commands

import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.sum
import java.math.BigDecimal

class CoinFlipBetStatsCommand(val plugin: HelpingHandsPlugin) : DiscordAbstractCommandBase(
		plugin.loritta,
		listOf("coinflip", "flipcoin", "girarmoeda", "caracoroa")
				.flatMap { listOf("$it bet stats", "$it apostar stats") },
		CommandCategory.ECONOMY
) {
	override fun command() = create {
		localizedDescription("commands.command.flipcoinbetstats.description")
		localizedExamples("commands.command.flipcoinbetstats.examples")

		usage {
			arguments {
				argument(ArgumentType.USER) {
					optional = true
				}
			}
		}

		executesDiscord {
			var checkStatsOfUser = user
			val checkStatsOfOtherUser = user(0)
			var checkingYourself = true

			if (checkStatsOfOtherUser != null) {
				checkStatsOfUser = validate(checkStatsOfOtherUser)
						.toJDA()
				checkingYourself = false
			}

			val winCount = loritta.newSuspendedTransaction {
				SonhosTransaction.select {
					(SonhosTransaction.receivedBy eq checkStatsOfUser.idLong) and
							(SonhosTransaction.reason eq SonhosPaymentReason.COIN_FLIP_BET)
				}.count()
			}

			val loseCount = loritta.newSuspendedTransaction {
				SonhosTransaction.select {
					(SonhosTransaction.givenBy eq checkStatsOfUser.idLong) and
							(SonhosTransaction.reason eq SonhosPaymentReason.COIN_FLIP_BET)
				}.count()
			}

			val playedCount = winCount + loseCount

			if (playedCount == 0L)
			// Never played!
				fail(locale["commands.command.flipcoinbetstats.playerHasNeverPlayed"])

			val sumField = SonhosTransaction.quantity.sum()
			val winSum = loritta.newSuspendedTransaction {
				SonhosTransaction.slice(sumField).select {
					(SonhosTransaction.receivedBy eq checkStatsOfUser.idLong) and
							(SonhosTransaction.reason eq SonhosPaymentReason.COIN_FLIP_BET)
				}.firstOrNull()?.get(sumField)
			} ?: BigDecimal.ZERO

			val loseSum = loritta.newSuspendedTransaction {
				SonhosTransaction.slice(sumField).select {
					(SonhosTransaction.givenBy eq checkStatsOfUser.idLong) and
							(SonhosTransaction.reason eq SonhosPaymentReason.COIN_FLIP_BET)
				}.firstOrNull()?.get(sumField)
			} ?: BigDecimal.ZERO

			val winPercentage = (winCount) / (winCount.toDouble() + loseCount.toDouble())
			val losePercentage = (loseCount) / (winCount.toDouble() + loseCount.toDouble())

			reply(
					LorittaReply(
							if (checkingYourself)
								locale["commands.command.flipcoinbetstats.yourStats"]
							else
								locale["commands.command.flipcoinbetstats.statsOfUser", checkStatsOfUser.asMention]
							,
							Emotes.LORI_RICH
					),
					LorittaReply(
							locale["commands.command.flipcoinbetstats.playedMatches", playedCount],
							mentionUser = false
					),
					LorittaReply(
							locale["commands.command.flipcoinbetstats.wonMatches", winPercentage, winCount],
							mentionUser = false
					),
					LorittaReply(
							locale["commands.command.flipcoinbetstats.lostMatches", losePercentage, loseCount],
							mentionUser = false
					),
					LorittaReply(
							locale["commands.command.flipcoinbetstats.wonSonhos", winSum],
							mentionUser = false
					),
					LorittaReply(
							locale["commands.command.flipcoinbetstats.lostSonhos", loseSum],
							mentionUser = false
					),
					LorittaReply(
							locale["commands.command.flipcoinbetstats.totalSonhos", winSum - loseSum],
							mentionUser = false
					),
					LorittaReply(
							locale["commands.command.flipcoinbetstats.probabilityExplanation"],
							Emotes.LORI_COFFEE,
							mentionUser = false
					),
					LorittaReply(
							locale["commands.command.flipcoinbetstats.bugsDoesntExist"],
							Emotes.LORI_BAN_HAMMER,
							mentionUser = false
					)
			)
		}
	}
}