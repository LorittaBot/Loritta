package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.tables.BomDiaECiaWinners
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.RankPaginationUtils
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import net.perfectdreams.loritta.morenitta.utils.extensions.getIconUrl
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import java.util.*
import kotlin.math.ceil

class BomDiaECiaCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Bomdiaecia
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.SOCIAL, UUID.fromString("0b2dca4f-7b8e-4f7e-9bf6-6b9d0e0a4f01")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        enableLegacyMessageSupport = true

        subcommand(I18N_PREFIX.Status.Label, I18N_PREFIX.Status.Description, UUID.fromString("3d5d4b54-9c78-4b1f-8db8-1ee2d6a7b4c1")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("bomdiaecia status")
                add("bd&c status")
                add("bdc status")
            }

            executor = StatusExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Top.Label, I18N_PREFIX.Top.Description, UUID.fromString("c0b1f8c5-0f64-4cf9-8a32-d2b1f0d5e9a7")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("bomdiaecia top")
                add("bd&c top")
                add("bdc top")
            }

            executor = TopExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Toplocal.Label, I18N_PREFIX.Toplocal.Description, UUID.fromString("8f4a7e3a-5d6e-4a9b-b1f3-7a2e9d3c1b88")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("bomdiaecia top local")
                add("bd&c top local")
                add("bdc top local")
            }

            executor = TopLocalExecutor(loritta)
        }
    }

    class StatusExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = optionalUser("user", I18N_PREFIX.Status.Options.User.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val user = args[options.user]?.user ?: context.user

            val wins = loritta.newSuspendedTransaction {
                BomDiaECiaWinners.selectAll().where { BomDiaECiaWinners.userId eq user.idLong }.count()
            }

            context.reply(false) {
                styled(
                    if (user == context.user) {
                        context.i18nContext.get(I18N_PREFIX.Status.YouWins(wins = wins))
                    } else {
                        context.i18nContext.get(I18N_PREFIX.Status.UserWins(userMention = user.asMention, wins = wins))
                    }
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(
                options.user to context.getUserAndMember(0)
            )
        }
    }

    class TopExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val page = optionalLong("page", I18N_PREFIX.Top.Options.Page.Text, RankingGenerator.VALID_RANKING_PAGES)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val userPage = args[options.page] ?: 1L
            val page = userPage - 1

            val message = createRankMessage(context, page)

            context.reply(false) {
                message()
            }
        }

        suspend fun createRankMessage(
            context: UnleashedContext,
            page: Long
        ): suspend InlineMessage<*>.() -> (Unit) = {
            val userId = BomDiaECiaWinners.userId
            val userIdCount = BomDiaECiaWinners.userId.count()

            val (totalCount, userData) = loritta.newSuspendedTransaction {
                val total = BomDiaECiaWinners.select(userId)
                    .groupBy(userId)
                    .count()

                val rows = BomDiaECiaWinners.select(userId, userIdCount)
                    .groupBy(userId)
                    .orderBy(userIdCount, SortOrder.DESC)
                    .limit(5)
                    .offset(page * 5)
                    .toList()

                Pair(total, rows)
            }

            val maxPage = ceil(totalCount / 5.0)

            val rankingImage = RankingGenerator.generateRanking(
                loritta,
                page * 5,
                context.i18nContext.get(I18N_PREFIX.Top.GlobalRanking),
                null,
                userData.map {
                    RankingGenerator.UserRankInformation(
                        it[userId],
                        context.i18nContext.get(I18N_PREFIX.Top.WonMatches(count = it[userIdCount]))
                    )
                }
            )

            RankPaginationUtils.createRankMessage(
                loritta,
                context,
                page,
                maxPage.toInt(),
                rankingImage
            ) {
                createRankMessage(context, it)
            }.invoke(this)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val page = args.getOrNull(0)?.toLongOrNull()

            if (page != null && !RankingGenerator.isValidRankingPage(page)) {
                context.reply(false) {
                    styled(
                        context.locale["commands.invalidRankingPage"],
                        Constants.ERROR
                    )
                }
                return null
            }

            return mapOf(options.page to page)
        }
    }

    class TopLocalExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val page = optionalLong("page", I18N_PREFIX.Toplocal.Options.Page.Text, RankingGenerator.VALID_RANKING_PAGES)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val guild = context.guild

            context.deferChannelMessage(false)

            val userPage = args[options.page] ?: 1L
            val page = userPage - 1

            val message = createRankMessage(context, guild, page)

            context.reply(false) {
                message()
            }
        }

        suspend fun createRankMessage(
            context: UnleashedContext,
            guild: Guild,
            page: Long
        ): suspend InlineMessage<*>.() -> (Unit) = {
            val userId = BomDiaECiaWinners.userId
            val userIdCount = BomDiaECiaWinners.userId.count()

            val (totalCount, userData) = loritta.newSuspendedTransaction {
                val total = BomDiaECiaWinners.innerJoin(GuildProfiles, { GuildProfiles.userId }, { userId })
                    .select(userId)
                    .where {
                        GuildProfiles.guildId eq guild.idLong and (GuildProfiles.isInGuild eq true)
                    }
                    .groupBy(userId)
                    .count()

                val rows = BomDiaECiaWinners.innerJoin(GuildProfiles, { GuildProfiles.userId }, { userId })
                    .select(userId, userIdCount)
                    .where {
                        GuildProfiles.guildId eq guild.idLong and (GuildProfiles.isInGuild eq true)
                    }
                    .groupBy(userId)
                    .orderBy(userIdCount, SortOrder.DESC)
                    .limit(5)
                    .offset(page * 5)
                    .toList()

                Pair(total, rows)
            }

            val maxPage = ceil(totalCount / 5.0)

            val rankingImage = RankingGenerator.generateRanking(
                loritta,
                page * 5,
                guild.name,
                guild.getIconUrl(256, ImageFormat.PNG),
                userData.map {
                    RankingGenerator.UserRankInformation(
                        it[userId],
                        context.i18nContext.get(I18N_PREFIX.Top.WonMatches(count = it[userIdCount]))
                    )
                }
            )

            RankPaginationUtils.createRankMessage(
                loritta,
                context,
                page,
                maxPage.toInt(),
                rankingImage
            ) {
                createRankMessage(context, guild, it)
            }.invoke(this)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val page = args.getOrNull(0)?.toLongOrNull()

            if (page != null && !RankingGenerator.isValidRankingPage(page)) {
                context.reply(false) {
                    styled(
                        context.locale["commands.invalidRankingPage"],
                        Constants.ERROR
                    )
                }
                return null
            }

            return mapOf(options.page to page)
        }
    }
}
