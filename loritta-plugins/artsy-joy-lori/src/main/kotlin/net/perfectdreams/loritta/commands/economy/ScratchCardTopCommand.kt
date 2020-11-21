package net.perfectdreams.loritta.commands.economy

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.tables.Raspadinhas
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction

class ScratchCardTopCommand : LorittaCommand(arrayOf("scratchcard top", "raspadinha top"), CommandCategory.ECONOMY) {
	override fun getDescription(locale: BaseLocale): String? {
		return locale["commands.economy.scratchcardtop.description"]
	}

	override fun getExamples(locale: BaseLocale): List<String> {
		return listOf()
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.NUMBER) {
				optional = true
			}
		}
	}

	@Subcommand
	suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
		var page = context.args.getOrNull(0)?.toLongOrNull()

		if (page != null && !RankingGenerator.isValidRankingPage(page)) {
			context.reply(
					LorittaReply(
							locale["commands.invalidRankingPage"],
							Constants.ERROR
					)
			)
			return
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

		context.sendFile(
				RankingGenerator.generateRanking(
						"Ranking Global",
						null,
						userData.map {
							RankingGenerator.UserRankInformation(
									it[userId],
									locale["commands.economy.scratchcardtop.wonTickets", it[moneySum].toString(), it[ticketCount].toString()]
							)
						}
				),
				"rank.png",
				context.getAsMention(true)
		)
	}
}