package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class RankCommand : AbstractCommand("rank", listOf("top", "leaderboard", "ranking"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["RANK_DESCRIPTION"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var page = context.args.getOrNull(0)?.toLongOrNull()

		if (page != null)
			page -= 1

		if (page == null)
			page = 0

		val profiles = transaction(Databases.loritta) {
			GuildProfiles.select {
				(GuildProfiles.guildId eq context.guild.idLong) and
						(GuildProfiles.isInGuild eq true)
			}
					.orderBy(GuildProfiles.xp to SortOrder.DESC)
					.limit(5, page * 5)
					.let { GuildProfile.wrapRows(it) }
					.toList()
		}

		logger.trace { "Retrived local profiles" }
		logger.trace { "profiles.size = ${profiles.size}" }

		context.sendFile(
				RankingGenerator.generateRanking(
						context.guild.name,
						context.guild.iconUrl,
						profiles.map {
							RankingGenerator.UserRankInformation(
									it.userId,
									"XP total // " + it.xp,
									"Nível " + it.getCurrentLevel().currentLevel
							)
						}
				) {
					newSuspendedTransaction {
						GuildProfiles.update({ GuildProfiles.id eq it and (GuildProfiles.guildId eq context.guild.idLong) }) {
							it[isInGuild] = false
						}
					}
					null
				},
				"rank.png",
				context.getAsMention(true)
		)
	}
}