package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class SonhosTopCommand : AbstractCommand("sonhostop", listOf("topsonhos"), CommandCategory.SOCIAL) {
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

		val userData = transaction(Databases.loritta) {
			Profiles.selectAll().orderBy(Profiles.money, SortOrder.DESC).limit(5, page * 5)
					.toList()
		}

		context.sendFile(
				RankingGenerator.generateRanking(
						"Ranking Global",
						null,
						userData.map {
							RankingGenerator.UserRankInformation(
									it[Profiles.id].value,
									"${it[Profiles.money]} sonhos"
							)
						}
				),
				"rank.png",
				context.getAsMention(true)
		)
	}
}