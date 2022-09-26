package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.morenitta.tables.GuildProfiles
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.api.commands.arguments
import net.perfectdreams.loritta.common.messages.LorittaReply
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.tables.BomDiaECiaWinners
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import org.jetbrains.exposed.sql.*

class BomDiaECiaTopLocalCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("bomdiaecia top local", "bd&c top local", "bdc top local"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.bomdiaeciatoplocal"
    }
    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")

        arguments {
            argument(ArgumentType.NUMBER) {
                optional = true
            }
        }

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

            val userId = BomDiaECiaWinners.userId
            val userIdCount = BomDiaECiaWinners.userId.count()

            val userData = loritta.newSuspendedTransaction {
                BomDiaECiaWinners.innerJoin(GuildProfiles, { GuildProfiles.userId }, { userId })
                    .slice(userId, userIdCount)
                    .select {
                        GuildProfiles.guildId eq guild.idLong and (GuildProfiles.isInGuild eq true)
                    }
                    .groupBy(userId)
                    .orderBy(userIdCount, SortOrder.DESC)
                    .limit(5, page * 5)
                    .toList()
            }

            sendImage(
                JVMImage(
                    RankingGenerator.generateRanking(
                        guild.name,
                        guild.iconUrl,
                        userData.map {
                            RankingGenerator.UserRankInformation(
                                it[userId],
                                locale["commands.command.bomdiaeciatop.wonMatches", it[userIdCount]]
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