package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.sonhosrank

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.rest.Image
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButtonWithHybridData
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.RankingGenerator
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import org.jetbrains.exposed.sql.*
import kotlin.math.ceil

class SonhosRankExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val rankType = string("rank_type", SonhosCommand.SONHOS_RANK_I18N_PREFIX.Options.RankType.Text) {
            choice(SonhosCommand.SONHOS_RANK_I18N_PREFIX.GlobalSonhosRank, SonhosRankType.GLOBAL.name)
            choice(SonhosCommand.SONHOS_RANK_I18N_PREFIX.LocalSonhosRank, SonhosRankType.LOCAL.name)
        }

        val page = optionalInteger("page", SonhosCommand.SONHOS_RANK_I18N_PREFIX.Options.Page.Text) {
            range = RankingGenerator.VALID_RANKING_PAGES
        }
    }

    companion object {
        suspend fun createMessageGlobal(
            loritta: LorittaCinnamon,
            context: InteractionContext,
            page: Long
        ): suspend MessageBuilder.() -> (Unit) = {
            styled(
                context.i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.Page(page + 1)),
                Emotes.LoriReading
            )

            val (totalCount, profiles) = loritta.pudding.transaction {
                val profiles = Profiles
                    .select {
                        Profiles.id notInSubQuery UsersService.validBannedUsersList(System.currentTimeMillis())
                    }
                    .orderBy(Profiles.money, SortOrder.DESC)
                    .limit(5, page * 5)
                    .toList()

                Pair(
                    // TODO: Fix the total Count, however there isn't an easy way to fix this
                    // While you could be thinking "well, I can just use a SELECT COUNT(*) FROM profiles query!", that gonna be very resource intensive on the db side.
                    // Because Loritta has a looooooooot of profiles
                    // (Besides, Loritta will always have more than (pageSize * RankingGenerator.VALID_RANKING_PAGES.last) profiles, heh
                    5 * RankingGenerator.VALID_RANKING_PAGES.last,
                    profiles
                )
            }

            // Calculates the max page
            val maxPage = ceil(totalCount / 5.0)
            val maxPageZeroIndexed = maxPage - 1

            addFile(
                "rank.png",
                RankingGenerator.generateRanking(
                    loritta,
                    page * 5,
                    context.i18nContext.get(SonhosCommand.SONHOS_RANK_I18N_PREFIX.GlobalSonhosRank),
                    null,
                    profiles.map {
                        RankingGenerator.UserRankInformation(
                            Snowflake(it[Profiles.id].value),
                            context.i18nContext.get(I18nKeysData.Commands.SonhosWithQuantity(it[Profiles.money]))
                        )
                    }
                ).toByteArray(ImageFormatType.PNG).inputStream()
            )

            actionRow {
                // The "page" variable is zero indexed, that's why in the "disabled" section the checks seems... "wonky"
                // The "VALID_RANKING_PAGES" is not zero indexed!
                interactiveButtonWithHybridData(
                    loritta,
                    ButtonStyle.Primary,
                    ChangeSonhosRankPageButtonExecutor,
                    ChangeSonhosRankPageData(context.user.id, page - 1, SonhosRankType.GLOBAL)
                ) {
                    loriEmoji = Emotes.ChevronLeft
                    disabled = page !in RankingGenerator.VALID_RANKING_PAGES
                }

                interactiveButtonWithHybridData(
                    loritta,
                    ButtonStyle.Primary,
                    ChangeSonhosRankPageButtonExecutor,
                    ChangeSonhosRankPageData(context.user.id, page + 1, SonhosRankType.GLOBAL)
                ) {
                    loriEmoji = Emotes.ChevronRight
                    disabled = page + 2 !in RankingGenerator.VALID_RANKING_PAGES || page >= maxPageZeroIndexed
                }
            }
        }

        suspend fun createMessageLocal(
            loritta: LorittaCinnamon,
            context: InteractionContext,
            guild: Guild,
            page: Long
        ): suspend MessageBuilder.() -> (Unit) = {
            styled(
                context.i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.Page(page + 1)),
                Emotes.LoriReading
            )

            val (totalCount, profiles) = loritta.pudding.transaction {
                val totalCount = Profiles.innerJoin(GuildProfiles, { Profiles.id }, { GuildProfiles.userId })
                    .select {
                        GuildProfiles.guildId eq guild.id.toLong() and (GuildProfiles.isInGuild eq true)
                    }.count()

                val profilesInTheQuery = Profiles.innerJoin(GuildProfiles, { Profiles.id }, { GuildProfiles.userId })
                    .select {
                        GuildProfiles.guildId eq guild.id.toLong() and (GuildProfiles.isInGuild eq true) and (GuildProfiles.userId notInSubQuery UsersService.validBannedUsersList(System.currentTimeMillis()))
                    }
                    .orderBy(Profiles.money, SortOrder.DESC)
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
                    context.i18nContext.get(SonhosCommand.SONHOS_RANK_I18N_PREFIX.LocalSonhosRank),
                    guild.getIconUrl(Image.Format.PNG),
                    profiles.map {
                        RankingGenerator.UserRankInformation(
                            Snowflake(it[Profiles.id].value),
                            context.i18nContext.get(I18nKeysData.Commands.SonhosWithQuantity(it[Profiles.money]))
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
                    ChangeSonhosRankPageButtonExecutor,
                    ChangeSonhosRankPageData(context.user.id, page - 1, SonhosRankType.LOCAL)
                ) {
                    loriEmoji = Emotes.ChevronLeft
                    disabled = page !in RankingGenerator.VALID_RANKING_PAGES
                }

                interactiveButtonWithHybridData(
                    loritta,
                    ButtonStyle.Primary,
                    ChangeSonhosRankPageButtonExecutor,
                    ChangeSonhosRankPageData(context.user.id, page + 1, SonhosRankType.LOCAL)
                ) {
                    loriEmoji = Emotes.ChevronRight
                    disabled = page + 2 !in RankingGenerator.VALID_RANKING_PAGES || page >= maxPageZeroIndexed
                }
            }
        }
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val type = SonhosRankType.valueOf(args[options.rankType])

        context.deferChannelMessage()

        val userPage = args[options.page] ?: 1L
        val page = userPage - 1

        val message = if (type == SonhosRankType.LOCAL) {
            if (context !is GuildApplicationCommandContext)
                context.fail {
                    styled(
                        context.i18nContext.get(SonhosCommand.SONHOS_RANK_I18N_PREFIX.YouCantSeeTheLocalSonhosRankOutsideOfAServer),
                        Emotes.Error
                    )
                }

            createMessageLocal(loritta, context, loritta.kord.getGuild(context.guildId)!!, page)
        } else {
            createMessageGlobal(loritta, context, page)
        }

        context.sendMessage {
            message()
        }
    }
}
