package net.perfectdreams.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll

class RankGlobalCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(
	loritta,
	listOf("rank global", "top global", "leaderboard global", "ranking global"),
	CommandCategory.SOCIAL
) {

	override fun command() = create {
		localizedDescription("commands.social.rankglobal.description")

		arguments {
			argument(ArgumentType.NUMBER) {
				optional = true
			}
		}

		executesDiscord {
			val pageIndex = when (val value = args.getOrNull(0)?.toLongOrNull()?.coerceAtLeast(0)) {
				0L, null -> 0L
				else -> {
					if (!RankingGenerator.isValidRankingPage(value)) {
						reply(LorittaReply(locale["commands.invalidRankingPage"], Constants.ERROR))
						return@executesDiscord
					}
					value - 1
				}
			}

			val profiles = loritta.newSuspendedTransaction {
				Profiles.selectAll()
					.orderBy(Profiles.xp to SortOrder.DESC)
					.limit(5, pageIndex * 5)
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
								"XP total // ${it.xp}",
								"Nível ${it.getCurrentLevel().currentLevel}"
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