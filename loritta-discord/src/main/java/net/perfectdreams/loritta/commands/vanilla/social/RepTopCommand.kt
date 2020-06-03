package net.perfectdreams.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class RepTopCommand : LorittaCommand(arrayOf("rep top", "reputation top", "reputacao top", "reputação top"), category = CommandCategory.SOCIAL) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.social.topreputation.description"]
    }

    override fun getExamples(locale: BaseLocale): List<String> {
        return listOf(
                locale["commands.social.topreputation.received"],
                locale["commands.social.topreputation.given"],
                "${locale["commands.social.topreputation.received"]} 5",
                "${locale["commands.social.topreputation.given"]} 5"
        )
    }

    override fun getUsage(locale: BaseLocale): CommandArguments {
        return arguments {
            argument(ArgumentType.TEXT) {}
            argument(ArgumentType.NUMBER) {
                optional = true
            }
        }
    }

    @Subcommand
    suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
        val typeName = context.args.getOrNull(0)

        if (typeName == null) {
            context.reply(
                    LoriReply(
                            "${context.config.commandPrefix}${context.getCommandLabel()} ${locale["commands.social.topreputation.received"]}"
                    ),
                    LoriReply(
                            "${context.config.commandPrefix}${context.getCommandLabel()} ${locale["commands.social.topreputation.given"]}"
                    )
            )
            return
        }

        val type = if (typeName in loritta.locales.map { locale["commands.social.topreputation.given"].toLowerCase() }.distinct())
            TopOrder.MOST_GIVEN
        else
            TopOrder.MOST_RECEIVED

        var page = context.args.getOrNull(1)?.toLongOrNull()

        if (page != null)
            page -= 1

        if (page == null)
            page = 0

        val receivedBy = Reputations.receivedById
        val givenBy = Reputations.givenById
        val receivedByCount = Reputations.receivedById.count()
        val givenByCount = Reputations.givenById.count()

        val userData = transaction(Databases.loritta) {
            if (type == TopOrder.MOST_GIVEN) {
                Reputations.slice(givenBy, givenByCount)
                        .selectAll()
                        .groupBy(givenBy)
                        .orderBy(givenByCount, SortOrder.DESC)
                        .limit(5, page * 5)
                        .toMutableList()
            } else {
                Reputations.slice(receivedBy, receivedByCount)
                        .selectAll()
                        .groupBy(receivedBy)
                        .orderBy(receivedByCount, SortOrder.DESC)
                        .limit(5, page * 5)
                        .toMutableList()
            }
        }

        context.sendFile(
                RankingGenerator.generateRanking(
                        "Ranking Global",
                        null,
                        userData.map {
                            if (type == TopOrder.MOST_RECEIVED) {
                                RankingGenerator.UserRankInformation(
                                        it[receivedBy],
                                        locale["commands.social.topreputation.receivedReputations", it[receivedByCount]]
                                )
                            } else {
                                RankingGenerator.UserRankInformation(
                                        it[givenBy],
                                        locale["commands.social.topreputation.givenReputations", it[givenBy]]
                                )
                            }
                        }
                ),
                "rank.png",
                context.getAsMention(true)
        )
    }

    private enum class TopOrder {
        MOST_RECEIVED,
        MOST_GIVEN
    }
}