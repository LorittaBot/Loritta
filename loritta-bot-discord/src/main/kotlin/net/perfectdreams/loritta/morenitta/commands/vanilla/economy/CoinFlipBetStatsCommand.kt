package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransaction
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import java.math.BigDecimal

class CoinFlipBetStatsCommand(val m: LorittaBot) : DiscordAbstractCommandBase(
	m,
	listOf("coinflip", "flipcoin", "girarmoeda", "caracoroa")
		.flatMap { listOf("$it bet stats", "$it apostar stats") },
	net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY
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
				SonhosTransaction.selectAll().where {
					(SonhosTransaction.receivedBy eq checkStatsOfUser.idLong) and
							(SonhosTransaction.reason eq SonhosPaymentReason.COIN_FLIP_BET)
				}.count()
			}

			val loseCount = loritta.newSuspendedTransaction {
				SonhosTransaction.selectAll().where {
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
				SonhosTransaction.select(sumField).where { 
					(SonhosTransaction.receivedBy eq checkStatsOfUser.idLong) and
							(SonhosTransaction.reason eq SonhosPaymentReason.COIN_FLIP_BET)
				}.firstOrNull()?.get(sumField)
			} ?: BigDecimal.ZERO

			val loseSum = loritta.newSuspendedTransaction {
				SonhosTransaction.select(sumField).where { 
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