package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.morenitta.tables.GuildProfiles
import net.perfectdreams.loritta.morenitta.tables.Profiles
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import org.jetbrains.exposed.sql.*

class SonhosTopLocalCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("sonhos top local", "atm top local"), net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY) {
	override fun command() = create {
		localizedDescription("commands.command.sonhostoplocal.description")

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
				Profiles.innerJoin(GuildProfiles, { Profiles.id }, { GuildProfiles.userId })
					.select {
						GuildProfiles.guildId eq guild.idLong and (GuildProfiles.isInGuild eq true)
					}.orderBy(Profiles.money, SortOrder.DESC).limit(5, page * 5)
					.toList()
			}

			sendImage(
				JVMImage(
					RankingGenerator.generateRanking(
						loritta,
						page * 5,
						guild.name,
						guild.iconUrl,
						userData.map {
							RankingGenerator.UserRankInformationX(
								it[Profiles.id].value,
								"${it[Profiles.money]} sonhos"
							)
						}
					) {
						loritta.newSuspendedTransaction {
							GuildProfiles.update({ GuildProfiles.id eq it and (GuildProfiles.guildId eq guild.idLong) }) {
								it[isInGuild] = false
							}
						}
						null
					}
				),
				"rank.png"
			)
		}
	}
}