package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.cinnamon.pudding.tables.BomDiaECiaWinners
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

class BomDiaECiaTopCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("bomdiaecia top", "bd&c top", "bdc top"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
	override fun command() = create {
		localizedDescription("commands.command.bomdiaeciatop.description")

		arguments {
			argument(ArgumentType.NUMBER) {
				optional = true
			}
		}

		executesDiscord {
			var page = args.getOrNull(0)?.toLongOrNull()

			if (page != null && !RankingGenerator.isValidRankingPage(page)) {
				reply(
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

			val userId = BomDiaECiaWinners.userId
			val userIdCount = BomDiaECiaWinners.userId.count()

			val userData = loritta.newSuspendedTransaction {
				BomDiaECiaWinners.select(userId, userIdCount)
					.groupBy(userId)
					.orderBy(userIdCount, SortOrder.DESC)
					.limit(5).offset(page * 5)
					.toMutableList()
			}

			sendImage(
				JVMImage(
					RankingGenerator.generateRanking(
						loritta,
						page * 5,
						"Ranking Global",
						null,
						userData.map {
							RankingGenerator.UserRankInformation(
								it[userId],
								locale["commands.command.bomdiaeciatop.wonMatches", it[userIdCount]]
							)
						}
					)
				),
				"rank.png",
				getUserMention(true)
			)
		}
	}
}