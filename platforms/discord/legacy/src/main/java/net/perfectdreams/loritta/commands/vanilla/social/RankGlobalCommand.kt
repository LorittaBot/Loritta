package net.perfectdreams.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll

class RankGlobalCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("rank global", "top global", "leaderboard global", "ranking global"), CommandCategory.SOCIAL) {
	override fun command() = create {
		localizedDescription("commands.command.rankglobal.description")

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

			val profiles = loritta.newSuspendedTransaction {
				Profiles.selectAll()
						.orderBy(Profiles.xp to SortOrder.DESC)
						.limit(5, page * 5)
						.let { Profile.wrapRows(it) }
						.toList()
			}

			sendImage(
					JVMImage(
							RankingGenerator.generateRanking(
									"Ranking Global",
									null,
									profiles.map {
										RankingGenerator.UserRankInformation(
												it.userId,
												"XP total // " + it.xp,
												"Nível " + it.getCurrentLevel().currentLevel
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