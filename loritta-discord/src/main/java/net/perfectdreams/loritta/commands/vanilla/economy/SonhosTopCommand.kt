package net.perfectdreams.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.tables.Profiles
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll

object SonhosTopCommand {
	fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("sonhos top", "atm top"), CommandCategory.ECONOMY) {
		description { it["commands.economy.sonhostop.description"] }

		executesDiscord {
			var page = args.getOrNull(0)?.toLongOrNull()

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