package net.perfectdreams.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.tables.BomDiaECiaWinners
import net.perfectdreams.loritta.utils.RankingGenerator
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