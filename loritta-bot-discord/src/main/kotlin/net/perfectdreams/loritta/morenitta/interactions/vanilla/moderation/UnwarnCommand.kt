package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.tables.Warns
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.ModerationLogAction
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.AdminUtils
import net.perfectdreams.loritta.morenitta.dao.Warn
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import java.util.*

class UnwarnCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Unwarn
        private val LOCALE_PREFIX = "commands.command"
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("d7c1a5e3-4b2f-4e8a-9f6d-3a1b5c7e9d2f")) {
        this.enableLegacyMessageSupport = true
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS)
        this.botPermissions = setOf(Permission.BAN_MEMBERS, Permission.KICK_MEMBERS)

        alternativeLegacyLabels.apply {
            add("desavisar")
        }

        executor = UnwarnExecutor(loritta)
    }

    class UnwarnExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = string("user", I18N_PREFIX.Options.User.Text)
            val warnId = optionalString("warn_id", I18N_PREFIX.Options.WarnId.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val (users, _) = AdminUtils.checkAndRetrieveAllValidUsersFromString(context, args[options.user]) ?: return
            val user = users.first()

            val member = context.guild.retrieveMemberOrNull(user)

            if (member != null) {
                if (!AdminUtils.checkForPermissions(context, member))
                    return
            }

            val warns = loritta.newSuspendedTransaction {
                Warn.find { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }.sortedBy { it.receivedAt }.toList()
            }

            if (warns.isEmpty()) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.NoWarnsFound(warnListCommand = "`/warnlist`")),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriBonk
                    )
                }
                return
            }

            val warnIdArg = args[options.warnId]

            val settings = AdminUtils.retrieveModerationInfo(loritta, context.config)

            val punishLogMessage = AdminUtils.getPunishmentForMessage(
                loritta,
                settings,
                context.guild,
                PunishmentAction.UNWARN
            )

            if (warnIdArg == "all") {
                val removedWarnsCount = loritta.newSuspendedTransaction {
                    val count = Warns.deleteWhere { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }

                    repeat(count) {
                        loritta.pudding.moderationLogs.logPunishment(
                            context.guild.idLong,
                            user.idLong,
                            context.user.idLong,
                            ModerationLogAction.UNWARN,
                            null,
                            null
                        )
                    }

                    count
                }

                if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
                    val textChannel = context.guild.getGuildMessageChannelById(settings.punishLogChannelId)

                    if (textChannel != null && textChannel.canTalk()) {
                        val message = MessageUtils.generateMessageOrFallbackIfInvalid(
                            context.i18nContext,
                            punishLogMessage,
                            listOf(user, context.guild),
                            context.guild,
                            mutableMapOf(
                                "duration" to context.locale["$LOCALE_PREFIX.mute.forever"]
                            ) + AdminUtils.getStaffCustomTokens(context.user) + AdminUtils.getPunishmentCustomTokens(context.locale, context.i18nContext.get(I18nKeysData.Commands.Category.Moderation.ReasonNotGiven), "$LOCALE_PREFIX.unwarn"),
                            generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.MemberModerationUnwarn
                        )

                        textChannel.sendMessage(message).queue()
                    }
                }

                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.WarnsRemoved(warnsCount = removedWarnsCount)),
                        "\uD83C\uDF89"
                    )
                }
                return
            }

            val warnIndex = if (warnIdArg != null) {
                val parsed = warnIdArg.toIntOrNull()
                if (parsed == null) {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18nKeysData.Commands.InvalidNumber(number = warnIdArg)),
                            net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriBonk
                        )
                    }
                    return
                }
                parsed
            } else {
                warns.size
            }

            if (warnIndex > warns.size || 1 > warnIndex) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.NotEnoughWarns(warnIndex = warnIndex, warnListCommand = "`/warnlist`")),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriBonk
                    )
                }
                return
            }

            val selectedWarn = warns[warnIndex - 1]

            loritta.newSuspendedTransaction {
                selectedWarn.delete()

                loritta.pudding.moderationLogs.logPunishment(
                    context.guild.idLong,
                    user.idLong,
                    context.user.idLong,
                    ModerationLogAction.UNWARN,
                    null,
                    null
                )
            }

            if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
                val textChannel = context.guild.getGuildMessageChannelById(settings.punishLogChannelId)

                if (textChannel != null && textChannel.canTalk()) {
                    val message = MessageUtils.generateMessageOrFallbackIfInvalid(
                        context.i18nContext,
                        punishLogMessage,
                        listOf(user, context.guild),
                        context.guild,
                        mutableMapOf(
                            "duration" to context.locale["$LOCALE_PREFIX.mute.forever"]
                        ) + AdminUtils.getStaffCustomTokens(context.user) + AdminUtils.getPunishmentCustomTokens(context.locale, context.i18nContext.get(I18nKeysData.Commands.Category.Moderation.ReasonNotGiven), "$LOCALE_PREFIX.unwarn"),
                        generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.MemberModerationUnwarn
                    )

                    textChannel.sendMessage(message).queue()
                }
            }

            context.reply(false) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.WarnsRemoved(warnsCount = 1)) + " ${Emotes.LORI_HMPF}",
                    "\uD83C\uDF89"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val (users, _) = AdminUtils.checkAndRetrieveAllValidUsersFromMessages(context) ?: return null

            return mapOf(
                options.user to users.joinToString(" ") { it.asMention },
                options.warnId to args.getOrNull(1)
            )
        }
    }
}
