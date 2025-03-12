package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.cinnamon.pudding.tables.BomDiaECiaWinners
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import net.perfectdreams.loritta.morenitta.utils.extensions.getIconUrl
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.innerJoin

class BomDiaECiaTopLocalCommand(loritta: LorittaBot): DiscordAbstractCommandBase(loritta, listOf("bomdiaecia top local", "bd&c top local", "bdc top local"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
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
                    .select(userId, userIdCount)
                    .where {
                        GuildProfiles.guildId eq guild.idLong and (GuildProfiles.isInGuild eq true)
                    }
                    .groupBy(userId)
                    .orderBy(userIdCount, SortOrder.DESC)
                    .limit(5).offset(page * 5)
                    .toList()
            }

            sendImage(
                JVMImage(
                    RankingGenerator.generateRanking(
                        loritta,
                        page * 5,
                        guild.name,
                        guild.getIconUrl(256, ImageFormat.PNG),
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