package net.perfectdreams.loritta.commands.economy

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.tables.Raspadinhas
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction

class ScratchCardTopCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("scratchcard top", "raspadinha top"), CommandCategory.ECONOMY) {
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

			val userData = transaction(Databases.loritta) {
				Raspadinhas.slice(userId, ticketCount, moneySum)
						.selectAll()
						.groupBy(userId)
						.having {
							moneySum.isNotNull()
						}
						.orderBy(moneySum, SortOrder.DESC)
						.limit(5, page * 5)
						.toMutableList()
			}

			context.sendImage(
					JVMImage(
						RankingGenerator.generateRanking(
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