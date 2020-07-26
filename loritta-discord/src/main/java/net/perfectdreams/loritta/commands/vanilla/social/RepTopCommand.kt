package net.perfectdreams.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.tables.Reputations
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll

object RepTopCommand {
	fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("rep top", "reputation top", "reputacao top", "reputação top"), CommandCategory.SOCIAL) {
		description { it["commands.social.topreputation.description"] }

		examples {
			listOf(
					it["commands.social.topreputation.received"],
					it["commands.social.topreputation.given"],
					"${it["commands.social.topreputation.received"]} 5",
					"${it["commands.social.topreputation.given"]} 5"
			)
		}

		arguments {
			argument(ArgumentType.TEXT) {}
			argument(ArgumentType.NUMBER) {
				optional = true
			}
		}

		executesDiscord {
			val typeName = args.getOrNull(0)

			if (typeName == null) {
				reply(
						LorittaReply(
								"${serverConfig.commandPrefix}${executedCommandLabel} ${locale["commands.social.topreputation.received"]}"
						),
						LorittaReply(
								"${serverConfig.commandPrefix}${executedCommandLabel} ${locale["commands.social.topreputation.given"]}"
						)
				)
				return@executesDiscord
			}

			val type = if (typeName in loritta.locales.map { locale["commands.social.topreputation.given"].toLowerCase() }.distinct())
				TopOrder.MOST_GIVEN
			else
				TopOrder.MOST_RECEIVED

			var page = args.getOrNull(1)?.toLongOrNull()

			if (page != null)
				page -= 1

			if (page == null)
				page = 0

			val receivedBy = Reputations.receivedById
			val givenBy = Reputations.givenById
			val receivedByCount = Reputations.receivedById.count()
			val givenByCount = Reputations.givenById.count()

			val userData = loritta.newSuspendedTransaction {
				if (type == TopOrder.MOST_GIVEN) {
					Reputations.slice(givenBy, givenByCount)
							.selectAll()
							.groupBy(givenBy)
							.orderBy(givenByCount, SortOrder.DESC)
							.limit(5, page * 5)
							.toMutableList()
				} else {
					Reputations.slice(receivedBy, receivedByCount)
							.selectAll()
							.groupBy(receivedBy)
							.orderBy(receivedByCount, SortOrder.DESC)
							.limit(5, page * 5)
							.toMutableList()
				}
			}

			sendImage(
					JVMImage(
							RankingGenerator.generateRanking(
									"Ranking Global",
									null,
									userData.map {
										if (type == TopOrder.MOST_RECEIVED) {
											RankingGenerator.UserRankInformation(
													it[receivedBy],
													locale["commands.social.topreputation.receivedReputations", it[receivedByCount]]
											)
										} else {
											RankingGenerator.UserRankInformation(
													it[givenBy],
													locale["commands.social.topreputation.givenReputations", it[givenByCount]]
											)
										}
									}
							)
					),
					"rank.png",
					getUserMention(true)
			)
		}
	}

	private enum class TopOrder {
		MOST_RECEIVED,
		MOST_GIVEN
	}
}