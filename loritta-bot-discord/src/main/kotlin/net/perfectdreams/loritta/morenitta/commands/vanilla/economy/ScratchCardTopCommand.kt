package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.cinnamon.pudding.tables.Raspadinhas
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.sum

class ScratchCardTopCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("scratchcard top", "raspadinha top"), net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.scratchcardtop.description")

		usage {
			arguments {
				argument(ArgumentType.NUMBER) {
					optional = true
				}
			}
		}

		executesDiscord {
			val context = this

			var page = context.args.getOrNull(0)?.toLongOrNull()

			if (page != null && !RankingGenerator.isValidRankingPage(page)) {
				context.reply(
					LorittaReply(
						locale["commands.invalidRankingPage"],
						Constants.ERROR
					)
				)
				return@executesDiscord
			}

			if (page != null)
				page -= 1

			if (page == null)
				page = 0

			val userId = Raspadinhas.receivedById
			val ticketCount = Raspadinhas.receivedById.count()
			val moneySum = Raspadinhas.value.sum()

			val userData = loritta.pudding.transaction {
				Raspadinhas.select(userId, ticketCount, moneySum)
					.groupBy(userId)
					.having {
						moneySum.isNotNull()
					}
					.orderBy(moneySum, SortOrder.DESC)
					.limit(5)
					.offset(page * 5)
					.toMutableList()
			}

			context.sendImage(
				JVMImage(
					RankingGenerator.generateRanking(
						loritta,
						page * 5,
						"Ranking Global",
						null,
						userData.map {
							RankingGenerator.UserRankInformation(
								it[userId],
								locale["$LOCALE_PREFIX.scratchcardtop.wonTickets", it[moneySum].toString(), it[ticketCount].toString()]
							)
						}
					)),
				"rank.png",
				context.getUserMention(true)
			)
		}
	}
}