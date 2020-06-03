package net.perfectdreams.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class RankGlobalCommand : LorittaCommand(arrayOf("rank global", "top global", "leaderboard global", "ranking global"), category = CommandCategory.SOCIAL) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.social.rankglobal.description"]
    }

    @Subcommand
    suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
        var page = context.args.getOrNull(0)?.toLongOrNull()

        if (page != null)
            page -= 1

        if (page == null)
            page = 0

        val profiles = transaction(Databases.loritta) {
            Profiles.selectAll()
                    .orderBy(Profiles.xp to SortOrder.DESC)
                    .limit(5, page * 5)
                    .let { Profile.wrapRows(it) }
                    .toList()
        }

        logger.trace { "Retrived global profiles" }
        logger.trace { "profiles.size = ${profiles.size}" }

        context.sendFile(
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
                ),
                "rank.png",
                context.getAsMention(true)
        )
    }
}