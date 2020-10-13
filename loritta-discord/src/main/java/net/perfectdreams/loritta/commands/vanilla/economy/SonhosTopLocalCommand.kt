package net.perfectdreams.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Profiles
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select

class SonhosTopLocalCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("sonhos top local", "atm top local"), CommandCategory.ECONOMY) {
	override fun command() = create {
		localizedDescription("commands.economy.sonhostoplocal.description")

		executesDiscord {
			var page = args.getOrNull(0)?.toLongOrNull()

			if (page != null)
				page -= 1

			if (page == null)
				page = 0

			val userData = loritta.newSuspendedTransaction {
				Profiles.innerJoin(GuildProfiles, { Profiles.id }, { GuildProfiles.userId })
						.select { GuildProfiles.guildId eq guild.idLong }.orderBy(Profiles.money, SortOrder.DESC).limit(5, page * 5)
						.toList()
			}

			sendImage(
					JVMImage(
							RankingGenerator.generateRanking(
									guild.name,
									guild.iconUrl,
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