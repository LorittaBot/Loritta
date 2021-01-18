package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class RankCommand : AbstractCommand("rank", listOf("top", "leaderboard", "ranking"), CommandCategory.SOCIAL) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["commands.social.rank.description"]
    }

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }

    override fun needsToUploadFiles(): Boolean {
        return true
    }

    override suspend fun run(context: CommandContext,locale: BaseLocale) {
        val pageIndex = when(val value = context.args.getOrNull(0)?.toLongOrNull()?.coerceAtLeast(0)) {
            0L, null -> 0L
            else -> {
                if(!RankingGenerator.isValidRankingPage(value)) {
                    context.reply(LorittaReply(context.locale["commands.invalidRankingPage"], Constants.ERROR))
                    return
                }
                value - 1
            }
        }

        val profiles = loritta.newSuspendedTransaction {
            GuildProfiles.select { (GuildProfiles.guildId eq context.guild.idLong) and (GuildProfiles.isInGuild eq true) }
                .orderBy(GuildProfiles.xp to SortOrder.DESC)
                .limit(5, pageIndex * 5)
                .let { GuildProfile.wrapRows(it) }
                .toList()
        }

        logger.trace {
            """
                Retrieved local profiles
                profiles.size = ${profiles.size}
            """.trimIndent()
        }

        context.sendFile(
            RankingGenerator.generateRanking(
                context.guild.name,
                context.guild.iconUrl,
                profiles.map {
                    RankingGenerator.UserRankInformation(
                        it.userId,
                        "XP total // ${it.xp}",
                        "Nível ${it.getCurrentLevel().currentLevel}"
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