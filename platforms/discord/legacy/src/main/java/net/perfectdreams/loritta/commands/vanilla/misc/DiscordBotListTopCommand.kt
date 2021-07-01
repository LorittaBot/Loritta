package net.perfectdreams.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.tables.BotVotes
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll

class DiscordBotListTopCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("dbl top"), CommandCategory.MISC) {
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
                BotVotes.slice(userId, userIdCount)
                    .selectAll()
                    .groupBy(userId)
                    .orderBy(userIdCount, SortOrder.DESC)
                    .limit(5, page * 5)
                    .toList()
            }

            sendImage(
                JVMImage(
                    RankingGenerator.generateRanking(
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