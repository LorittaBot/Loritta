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
import net.perfectdreams.loritta.cinnamon.pudding.tables.Warns
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.AdminUtils
import net.perfectdreams.loritta.morenitta.dao.Warn
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
import org.jetbrains.exposed.sql.and
import java.time.Instant
import java.util.*
import kotlin.math.ceil

class WarnListCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Warnlist
        private const val WARNS_PER_PAGE = 5
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("9c4b8e21-3f5d-4a7e-b8c2-1d9f6e4a3b5c")) {
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS)
        this.enableLegacyMessageSupport = true

        alternativeLegacyLabels.apply {
            add("punishmentlist")
            add("listadeavisos")
            add("infractions")
            add("warns")
        }

        executor = WarnListExecutor(loritta)
    }

    class WarnListExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
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

        private suspend fun processMessage(
            context: UnleashedContext,
            user: User,
            requestedPage: Int
        ) {
            val warns = loritta.newSuspendedTransaction {
                Warn.find { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }
                    .sortedByDescending { it.receivedAt }
                    .toList()
            }

            if (warns.isEmpty()) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.UserDoesntHaveWarns(userMention = user.asMention)),
                        Emotes.LoriSob
                    )
                }
                return
            }

            val totalPages = ceil(warns.size.toDouble() / WARNS_PER_PAGE).toInt()
            val page = requestedPage.coerceIn(0, totalPages - 1)

            val startIndex = page * WARNS_PER_PAGE
            val endIndex = minOf(startIndex + WARNS_PER_PAGE, warns.size)
            val warnsForPage = warns.subList(startIndex, endIndex)

            val warnPunishments = AdminUtils.retrieveWarnPunishmentActions(loritta, context.config)
            val now = Instant.now()
            val activeWarnCount = warns.count { it.expiresAt == null || it.expiresAt!!.isAfter(now) }
            val nextPunishment = warnPunishments.firstOrNull { it.warnCount == activeWarnCount + 1 }

            context.reply(false) {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColorRaw = LorittaColors.LorittaRed.rgb

                    this.components += Section(Thumbnail(user.effectiveAvatarUrl)) {
                        this.components += TextDisplay(
                            buildString {
                                append("### 🚔 ${context.i18nContext.get(I18N_PREFIX.Title(userMention = user.asMention))}")
                                append(" — ${context.i18nContext.get(I18N_PREFIX.Page(page = page + 1))}")
                                appendLine()
                                appendLine()

                                warnsForPage.forEachIndexed { idx, warn ->
                                    val warnNumber = startIndex + idx + 1
                                    val warnExpiresAt = warn.expiresAt

                                    appendLine(context.i18nContext.get(I18N_PREFIX.WarnEntry(warnNumber = warnNumber)))
                                    appendLine(context.i18nContext.get(I18N_PREFIX.PunishedBy(punisherMention = "<@${warn.punishedById}>")))
                                    appendLine(context.i18nContext.get(I18N_PREFIX.Reason(reason = warn.content ?: context.i18nContext.get(I18nKeysData.Commands.Category.Moderation.ReasonNotGiven))))
                                    appendLine(context.i18nContext.get(I18N_PREFIX.Date(date = DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(warn.receivedAt))))

                                    if (warnExpiresAt != null) {
                                        val expirationLabel = if (warnExpiresAt.isAfter(now))
                                            I18N_PREFIX.Expires(expiresAt = "<t:${warnExpiresAt.epochSecond}:R>")
                                        else
                                            I18N_PREFIX.Expired(expiredAt = "<t:${warnExpiresAt.epochSecond}:R>")
                                        appendLine(context.i18nContext.get(expirationLabel))
                                    }

                                    appendLine()
                                }

                                if (nextPunishment != null) {
                                    val punishmentType = context.i18nContext.get(
                                        when (nextPunishment.punishmentAction) {
                                            PunishmentAction.BAN -> I18nKeysData.Commands.Command.Modlog.Actions.Ban
                                            PunishmentAction.KICK -> I18nKeysData.Commands.Command.Modlog.Actions.Kick
                                            PunishmentAction.MUTE -> I18nKeysData.Commands.Command.Modlog.Actions.Mute
                                            else -> throw RuntimeException("Punishment $nextPunishment is not supported")
                                        }
                                    ).lowercase()
                                    appendLine("-# ${context.i18nContext.get(I18N_PREFIX.NextPunishment(punishmentType = punishmentType))}")
                                }

                                append("-# ${context.i18nContext.get(I18N_PREFIX.Footer(totalWarns = warns.size))}")
                            }
                        )
                    }
                }

                val addLeftButton = page > 0
                val addRightButton = page < totalPages - 1

                val prevButton = if (addLeftButton) {
                    loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        ButtonStyle.PRIMARY,
                        builder = {
                            loriEmoji = Emotes.ChevronLeft
                        }
                    ) {
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

                val nextButton = if (addRightButton) {
                    loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        ButtonStyle.PRIMARY,
                        builder = {
                            loriEmoji = Emotes.ChevronRight
                        }
                    ) {
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

            val page = args.getOrNull(1)?.toLongOrNull()

            return mapOf(
                options.user to userAndMember.user,
                options.page to page
            )
        }
    }
}
