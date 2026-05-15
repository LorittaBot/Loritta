package net.perfectdreams.loritta.morenitta.interactions.vanilla.misc

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
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
import java.util.UUID
import kotlin.math.ceil

class VoteCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Vote
    }

    override fun command() = slashCommand(
        I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MISC,
        uniqueId = UUID.fromString("f91c8c3e-31b3-4e7c-b17a-d5e9a9c8432c")
    ) {
        alternativeLegacyLabels.apply {
            add("vote")
            add("upvote")
            add("dbl")
        }
        enableLegacyMessageSupport = true

        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        val infoExecutor = DiscordBotListInfoExecutor()
        executor = infoExecutor

        subcommand(I18N_PREFIX.Info.Label, I18N_PREFIX.Info.Description, UUID.fromString("a4f04a4a-0e2a-4d70-9d2a-26e6b2c5bb27")) {
            executor = infoExecutor
        }

        subcommand(I18N_PREFIX.Status.Label, I18N_PREFIX.Status.Description, UUID.fromString("0d7d7d44-3d8e-4d8a-83f2-1d8cf9d0bcd1")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("votar status")
                add("vote status")
                add("dbl status")
                add("upvote status")
            }

            executor = DiscordBotListStatusExecutor()
        }

        subcommandGroup(I18N_PREFIX.Top.Label, I18N_PREFIX.Top.Description) {
            subcommand(I18N_PREFIX.Top.Global.Label, I18N_PREFIX.Top.Global.Description, UUID.fromString("5b1d0c1d-7c1a-4f2a-8c2e-0a9c7c1b9d11")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("votar top")
                    add("vote top")
                    add("dbl top")
                    add("upvote top")
                }

                executor = DiscordBotListTopGlobalExecutor()
            }

            subcommand(I18N_PREFIX.Top.Local.Label, I18N_PREFIX.Top.Local.Description, UUID.fromString("7e23c4fa-2b9b-4f3c-9c6d-c0a3a76b8e22")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("votar top local")
                    add("vote top local")
                    add("dbl top local")
                    add("upvote top local")
                }

                executor = DiscordBotListTopLocalExecutor()
            }
        }
    }

    inner class DiscordBotListInfoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.reply(false) {
                embed {
                    title = "✨ Discord Bot List"
                    color = Constants.LORITTA_AQUA.rgb
                    thumbnail = "${loritta.config.loritta.website.url}assets/img/loritta_star.png"
                    description = context.i18nContext.get(
                        I18N_PREFIX.Info.Message(
                            Emotes.DISCORD_BOT_LIST,
                            context.config.commandPrefix,
                            "https://top.gg/bot/${loritta.config.loritta.discord.applicationId}"
                        )
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }

    inner class DiscordBotListStatusExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = optionalUser("user", I18N_PREFIX.Status.Options.User.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val targetUser = args[options.user]?.user ?: context.user

            val votes = loritta.newSuspendedTransaction {
                BotVotes.selectAll().where { BotVotes.userId eq targetUser.idLong }.count()
            }

            context.reply(false) {
                if (targetUser == context.user) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Status.YouVoted(votes = votes))
                    )
                } else {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.Status.UserVoted(
                                userMention = targetUser.asMention,
                                votes = votes
                            )
                        )
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val userAndMember = context.getUserAndMember(0)
            return mapOf(options.user to userAndMember)
        }
    }

    inner class DiscordBotListTopGlobalExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val page = optionalLong("page", I18N_PREFIX.Top.Global.Options.Page.Text, RankingGenerator.VALID_RANKING_PAGES)
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
            val userId = BotVotes.userId
            val userIdCount = BotVotes.userId.count()

            val (totalCount, userData) = loritta.newSuspendedTransaction {
                val total = BotVotes.select(userId)
                    .groupBy(userId)
                    .count()

                val rows = BotVotes.select(userId, userIdCount)
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
                        context.i18nContext.get(I18N_PREFIX.Top.Votes(votes = it[userIdCount]))
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

    inner class DiscordBotListTopLocalExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val page = optionalLong("page", I18N_PREFIX.Top.Local.Options.Page.Text, RankingGenerator.VALID_RANKING_PAGES)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val guild = context.guild

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
            val guild = context.guild
            val userId = BotVotes.userId
            val userIdCount = BotVotes.userId.count()

            val (totalCount, userData) = loritta.newSuspendedTransaction {
                val total = BotVotes.innerJoin(GuildProfiles, { GuildProfiles.userId }, { userId })
                    .select(userId)
                    .where {
                        GuildProfiles.guildId eq guild.idLong and (GuildProfiles.isInGuild eq true)
                    }
                    .groupBy(userId)
                    .count()

                val rows = BotVotes.innerJoin(GuildProfiles, { GuildProfiles.userId }, { userId })
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
                        context.i18nContext.get(I18N_PREFIX.Top.Votes(votes = it[userIdCount]))
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
}
