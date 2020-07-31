package net.perfectdreams.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.tables.Profiles
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import net.perfectdreams.loritta.tables.BomDiaECiaWinners
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll

object BomDiaECiaTopCommand {
	fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("bomdiaecia top", "bd&c top"), CommandCategory.SOCIAL) {
		description { it["commands.social.bomdiaeciatop.description"] }

		arguments {
			argument(ArgumentType.NUMBER) {
				optional = true
			}
		}

		executesDiscord {
			var page = args.getOrNull(0)?.toLongOrNull()

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
												locale["commands.social.bomdiaeciatop.wonMatches", it[userIdCount]]
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