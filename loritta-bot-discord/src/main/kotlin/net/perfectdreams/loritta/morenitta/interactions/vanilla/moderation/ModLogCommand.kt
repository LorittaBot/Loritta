package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.Section
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.interactions.components.Thumbnail
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.services.ModerationLogsService.Companion.toPunishmentLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationLogs
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.ModerationLogAction
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
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.util.*
import kotlin.math.ceil

class ModLogCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Modlog
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("8f7a9e3d-1b2c-4d3e-9f4a-5e6b7c8d9a0b")) {
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS)
        this.enableLegacyMessageSupport = true

        executor = ModLogExecutor(loritta)
    }

    class ModLogExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        companion object {
            // Number of logs to display per page
            private const val LOGS_PER_PAGE = 10
        }

        inner class Options : ApplicationCommandOptions() {
            val user = user("user", I18N_PREFIX.Options.User.Text)
            val page = optionalLong("page", I18N_PREFIX.Options.Page.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val user = args[options.user].user
            val page = ((args[options.page] ?: 1L) - 1).coerceAtLeast(0).toInt()

            processMessage(context, user, page)
        }

        /**
         * Displays a specific page of logs
         *
         * @param context The command context
         * @param user The user whose logs are being displayed
         * @param page The page number (0-indexed)
         * @param totalPages The total number of pages
         */
        private suspend fun processMessage(
            context: UnleashedContext,
            user: User,
            page: Int
        ) {
            val (punishmentCount, logsForPage) = loritta.transaction {
                val logsForPage = ModerationLogs.selectAll().where {
                    (ModerationLogs.guildId eq context.guild.idLong) and (ModerationLogs.userId eq user.idLong)
                }.orderBy(ModerationLogs.timestamp, SortOrder.DESC)
                    .limit(LOGS_PER_PAGE)
                    .offset((page * LOGS_PER_PAGE).toLong())
                    .toList()
                    .map { it.toPunishmentLog() }

                val punishmentCount = ModerationLogs.selectAll().where {
                    (ModerationLogs.guildId eq context.guild.idLong) and (ModerationLogs.userId eq user.idLong)
                }.count()

                return@transaction Pair(punishmentCount, logsForPage)
            }

            if (punishmentCount == 0L) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.NoLogs(user.asMention)),
                        Emotes.LoriSob
                    )
                }
                return
            }

            val totalPages = ceil(punishmentCount.toDouble() / LOGS_PER_PAGE).toInt()

            context.reply(false) {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColorRaw = LorittaColors.LorittaRed.rgb

                    this.components += Section(Thumbnail(user.effectiveAvatarUrl)) {
                        this.components += TextDisplay(
                            buildString {
                                append("### ${context.i18nContext.get(I18N_PREFIX.Title(user.effectiveName))}")
                                append(" — ${context.i18nContext.get(I18N_PREFIX.Page(page + 1))}")
                                appendLine()

                                for (log in logsForPage) {
                                    // Try to get the punisher user
                                    val punisher = loritta.lorittaShards.retrieveUserInfoById(log.punisherId)
                                    val punisherName = punisher?.let { "<@${it.id}>" } ?: context.i18nContext.get(I18N_PREFIX.UnknownUser)

                                    val actionName = context.i18nContext.get(when (log.punishmentAction) {
                                        ModerationLogAction.BAN -> I18N_PREFIX.Actions.Ban
                                        ModerationLogAction.KICK -> I18N_PREFIX.Actions.Kick
                                        ModerationLogAction.MUTE -> I18N_PREFIX.Actions.Mute
                                        ModerationLogAction.WARN -> I18N_PREFIX.Actions.Warn
                                        ModerationLogAction.UNBAN -> I18N_PREFIX.Actions.Unban
                                        ModerationLogAction.UNMUTE -> I18N_PREFIX.Actions.Unmute
                                        ModerationLogAction.UNWARN -> I18N_PREFIX.Actions.Unwarn
                                    })

                                    appendLine(context.i18nContext.get(I18N_PREFIX.Type(actionName)))
                                    appendLine(context.i18nContext.get(I18N_PREFIX.PunishedBy(punisherName)))
                                    appendLine(context.i18nContext.get(I18N_PREFIX.PunishedAt(DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(log.timestamp))))
                                    appendLine(context.i18nContext.get(I18N_PREFIX.Reason(log.reason ?: context.i18nContext.get(I18N_PREFIX.NoReason))))

                                    val muteDuration = log.muteDuration
                                    if (muteDuration != null) {
                                        appendLine(context.i18nContext.get(I18N_PREFIX.ExpiresAt(DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(muteDuration))))
                                    }

                                    appendLine()
                                }

                                appendLine("-# ${context.i18nContext.get(I18N_PREFIX.Footer(punishmentCount))}")
                            }
                        )
                    }
                }

                // Add pagination buttons (always visible, but disabled when not usable)
                // Check if buttons should be enabled
                val addLeftButton = page > 0
                val addRightButton = page < totalPages - 1

                // Create previous page button
                val prevButton = if (addLeftButton) {
                    loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        ButtonStyle.PRIMARY,
                        builder = {
                            loriEmoji = Emotes.ChevronLeft
                        }
                    ) {
                        // Go to previous page
                        val newPage = 0.coerceAtLeast(page - 1)
                        it.deferEdit()
                        processMessage(it, user, newPage)
                    }
                } else {
                    loritta.interactivityManager.disabledButton(
                        context.alwaysEphemeral,
                        ButtonStyle.PRIMARY,
                        builder = {
                            loriEmoji = Emotes.ChevronLeft
                        }
                    )
                }

                // Create next page button
                val nextButton = if (addRightButton) {
                    loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        ButtonStyle.PRIMARY,
                        builder = {
                            loriEmoji = Emotes.ChevronRight
                        }
                    ) {
                        // Go to next page
                        val newPage = (totalPages - 1).coerceAtMost(page + 1)
                        it.deferEdit()
                        processMessage(it, user, newPage)
                    }
                } else {
                    loritta.interactivityManager.disabledButton(
                        context.alwaysEphemeral,
                        ButtonStyle.PRIMARY,
                        builder = {
                            loriEmoji = Emotes.ChevronRight
                        }
                    )
                }

                // Add buttons to the message
                this.components += ActionRow.of(prevButton, nextButton)
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val userAndMember = context.getUserAndMember(0)
            if (userAndMember == null) {
                context.explain()
                return null
            }

            // Check if there's a page argument (should be the second argument)
            val page = args.getOrNull(1)?.toLongOrNull()

            return mapOf(
                options.user to userAndMember.user,
                options.page to page
            )
        }
    }
}
