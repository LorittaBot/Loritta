package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.xprank

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.Guild
import dev.kord.rest.Image
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButtonWithHybridData
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.XpCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.ExperienceUtils
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import kotlin.math.ceil

class XpRankExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val page = optionalInteger("page", XpCommand.XP_RANK_I18N_PREFIX.Options.Page.Text) {
            range = RankingGenerator.VALID_RANKING_PAGES
        }
    }

    companion object {
        suspend fun createMessage(
            loritta: LorittaBot,
            context: InteractionContext,
            guild: Guild,
            page: Long
        ): suspend MessageBuilder.() -> (Unit) = {
            styled(
                context.i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.Page(page + 1)),
                Emotes.LoriReading
            )

            val (totalCount, profiles) = loritta.pudding.transaction {
                val totalCount = GuildProfiles.select {
                    (GuildProfiles.guildId eq guild.id.toLong()) and
                            (GuildProfiles.isInGuild eq true)
                }.count()

                val profilesInTheQuery = GuildProfiles.select {
                    (GuildProfiles.guildId eq guild.id.toLong()) and
                            (GuildProfiles.isInGuild eq true)
                }
                    .orderBy(GuildProfiles.xp to SortOrder.DESC)
                    .limit(5, page * 5)
                    .toList()

                Pair(totalCount, profilesInTheQuery)
            }

            // Calculates the max page
            val maxPage = ceil(totalCount / 5.0)
            val maxPageZeroIndexed = maxPage - 1

            addFile(
                "rank.png",
                RankingGenerator.generateRanking(
                    loritta,
                    page * 5,
                    guild.name,
                    guild.getIconUrl(Image.Format.PNG),
                    profiles.map {
                        val xp = it[GuildProfiles.xp]
                        val level = ExperienceUtils.getCurrentLevelForXp(xp)

                        RankingGenerator.UserRankInformation(
                            it[GuildProfiles.userId],
                            context.i18nContext.get(XpCommand.XP_RANK_I18N_PREFIX.TotalXpAndLevel(xp, level))
                        )
                    }
                ) {
                    loritta.pudding.transaction {
                        GuildProfiles.update({ GuildProfiles.id eq it.toLong() and (GuildProfiles.guildId eq guild.id.toLong()) }) {
                            it[isInGuild] = false
                        }
                    }
                    null
                }.toByteArray(ImageFormatType.PNG).inputStream()
            )

            actionRow {
                // The "page" variable is zero indexed, that's why in the "disabled" section the checks seems... "wonky"
                // The "VALID_RANKING_PAGES" is not zero indexed!
                interactiveButtonWithHybridData(
                    loritta,
                    ButtonStyle.Primary,
                    ChangeXpRankPageButtonExecutor,
                    ChangeXpRankPageData(context.user.id, page - 1)
                ) {
                    loriEmoji = Emotes.ChevronLeft
                    disabled = page !in RankingGenerator.VALID_RANKING_PAGES
                }

                interactiveButtonWithHybridData(
                    loritta,
                    ButtonStyle.Primary,
                    ChangeXpRankPageButtonExecutor,
                    ChangeXpRankPageData(context.user.id, page + 1)
                ) {
                    loriEmoji = Emotes.ChevronRight
                    disabled = page + 2 !in RankingGenerator.VALID_RANKING_PAGES || page >= maxPageZeroIndexed
                }
            }
        }
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            return

        context.deferChannelMessage()

        val guild = loritta.kord.getGuild(context.guildId)!!

        val userPage = args[options.page] ?: 1L
        val page = userPage - 1

        val message = createMessage(loritta, context, guild, page)

        context.sendMessage {
            message()
        }
    }
}
