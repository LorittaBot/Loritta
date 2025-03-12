package net.perfectdreams.loritta.morenitta.commands.vanilla.misc

import net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotes
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count

class DiscordBotListTopCommand(loritta: LorittaBot): DiscordAbstractCommandBase(loritta, listOf("dbl top"), net.perfectdreams.loritta.common.commands.CommandCategory.MISC) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.dbltop"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")

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

            val userId = BotVotes.userId
            val userIdCount = BotVotes.userId.count()

            val userData = loritta.newSuspendedTransaction {
                BotVotes.select(userId, userIdCount)
                    .groupBy(userId)
                    .orderBy(userIdCount, SortOrder.DESC)
                    .limit(5)
                    .offset(page * 5)
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
                            RankingGenerator.UserRankInformation(
                                it[userId],
                                locale["$LOCALE_PREFIX.votes", it[userIdCount]]
                            )
                        }
                    )
                ),
                "rank.png",
                getUserMention(true)
            )
        }
    }
}