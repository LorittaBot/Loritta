package net.perfectdreams.loritta.legacy.commands.vanilla.social

import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.api.commands.ArgumentType
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.api.commands.arguments
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.api.utils.image.JVMImage
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.legacy.tables.BomDiaECiaWinners
import net.perfectdreams.loritta.legacy.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll

class BomDiaECiaTopCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("bomdiaecia top", "bd&c top", "bdc top"), CommandCategory.SOCIAL) {
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
				BomDiaECiaWinners.slice(userId, userIdCount)
						.selectAll()
						.groupBy(userId)
						.orderBy(userIdCount, SortOrder.DESC)
						.limit(5, page * 5)
						.toMutableList()
			}

			sendImage(
					JVMImage(
							RankingGenerator.generateRanking(
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