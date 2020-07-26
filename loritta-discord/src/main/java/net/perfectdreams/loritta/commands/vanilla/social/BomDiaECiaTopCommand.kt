package net.perfectdreams.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.tables.BomDiaECiaWinners
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll

class BomDiaECiaTopCommand : LorittaCommand(arrayOf("bomdiaecia top", "bd&c top"), category = CommandCategory.SOCIAL) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.social.bomdiaeciatop.description"]
    }

    override fun getExamples(locale: BaseLocale): List<String> {
        return listOf()
    }

    override fun getUsage(locale: BaseLocale): CommandArguments {
        return arguments {
            argument(ArgumentType.NUMBER) {
                optional = true
            }
        }
    }

    @Subcommand
    suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
        var page = context.args.getOrNull(0)?.toLongOrNull()

        if (page != null)
            page -= 1

        if (page == null)
            page = 0

        val userId = BomDiaECiaWinners.userId
        val userIdCount = BomDiaECiaWinners.userId.count()

        val userData = loritta.newSuspendedTransaction {
            BomDiaECiaWinners.slice(userId, userIdCount)
                    .selectAll()
                    .groupBy(userId)
                    .orderBy(userIdCount, SortOrder.DESC)
                    .limit(5, page * 5)
                    .toMutableList()
        }

        context.sendFile(
                RankingGenerator.generateRanking(
                        "Ranking Global",
                        null,
                        userData.map {
                            RankingGenerator.UserRankInformation(
                                    it[userId],
                                    locale["commands.social.bomdiaeciatop.wonMatches", it[userIdCount]]
                            )
                        }
                ),
                "rank.png",
                context.getAsMention(true)
        )
    }
}