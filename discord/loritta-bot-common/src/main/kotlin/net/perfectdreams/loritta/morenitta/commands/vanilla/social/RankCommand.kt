package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.GuildProfile
import net.perfectdreams.loritta.morenitta.tables.GuildProfiles
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import net.perfectdreams.loritta.morenitta.LorittaBot

class RankCommand(loritta: LorittaBot) : AbstractCommand(
    loritta,
    "rank",
    listOf("top", "leaderboard", "ranking"),
    net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL
) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.rank.description")

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }

    override fun needsToUploadFiles(): Boolean {
        return true
    }

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "xp rank")

        var page = context.args.getOrNull(0)?.toLongOrNull()

        if (page != null && !RankingGenerator.isValidRankingPage(page)) {
            context.reply(
                LorittaReply(
                    context.locale["commands.invalidRankingPage"],
                    Constants.ERROR
                )
            )
            return
        }

        if (page != null)
            page -= 1

        if (page == null)
            page = 0

        val profiles = loritta.newSuspendedTransaction {
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
                loritta,
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
                loritta.newSuspendedTransaction {
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