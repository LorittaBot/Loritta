package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.morenitta.tables.Profiles
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll

class SonhosTopCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("sonhos top", "atm top"), net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY) {
	override fun command() = create {
		localizedDescription("commands.command.sonhostop.description")

		executesDiscord {
			OutdatedCommandUtils.sendOutdatedCommandMessage(this, locale, "sonhos rank")

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
						loritta,
						page * 5,
						"Ranking Global",
						null,
						userData.map {
							RankingGenerator.UserRankInformationX(
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