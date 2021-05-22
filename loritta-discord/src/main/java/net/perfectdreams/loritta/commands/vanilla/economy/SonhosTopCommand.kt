package net.perfectdreams.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll

class SonhosTopCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("sonhos top", "atm top"), CommandCategory.ECONOMY) {
	override fun command() = create {
		localizedDescription("commands.command.sonhostop.description")

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

			val userData = loritta.newSuspendedTransaction {
				Profiles.selectAll().orderBy(Profiles.money, SortOrder.DESC).limit(5, page * 5)
						.toList()
			}

			sendImage(
					JVMImage(
							RankingGenerator.generateRanking(
									"Ranking Global",
									null,
									userData.map {
										RankingGenerator.UserRankInformation(
												it[Profiles.id].value,
												"${it[Profiles.money]} sonhos"
										)
									}
							)
					),
					"rank.png"
			)
		}
	}
}