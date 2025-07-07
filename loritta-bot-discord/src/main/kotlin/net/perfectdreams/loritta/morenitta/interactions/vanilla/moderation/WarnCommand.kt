package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonParser
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.pudding.tables.Warns
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.ModerationLogAction
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.AdminUtils
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.BanCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.KickCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.MuteCommand
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
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.TimeUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class WarnCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Warn
        val CATEGORY_I18N_PREFIX = I18nKeysData.Commands.Category.Moderation
        private const val LOCALE_PREFIX = "commands.command"
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("f5bffe2b-59f0-428e-86b1-ef948eaa91ab")) {
        this.enableLegacyMessageSupport = true
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS)
        this.botPermissions = setOf(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)

        alternativeLegacyLabels.apply {
            add("aviso")
            add("avisar")
        }

        executor = WarnExecutor(loritta)
    }

    class WarnExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            // May be multiple in the same string
            val users = string("users", CATEGORY_I18N_PREFIX.Options.Users.Text)

            val reason = optionalString("reason", CATEGORY_I18N_PREFIX.Options.Reason.Text) {
                // TODO: Add this back
                // allowedLength = 0..512
            }

            val skipConfirmation = optionalBoolean("skip_confirmation", CATEGORY_I18N_PREFIX.Options.SkipConfirmation.Text)
            val isSilent = optionalBoolean("is_silent", CATEGORY_I18N_PREFIX.Options.IsSilent.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val (users, rawReason) = AdminUtils.checkAndRetrieveAllValidUsersFromString(context, args[options.users]) ?: return

            for (user in users) {
                val member = context.guild.retrieveMemberOrNull(user)

                if (member != null) {
                    if (!AdminUtils.checkForPermissions(context, member))
                        return
                }
            }

            // Technically, because the settings are already "pre-baked", we don't need to do stuff
            val reason = (args[options.reason] ?: "").ifBlank { context.i18nContext.get(I18nKeysData.Commands.Category.Moderation.ReasonNotGiven) }
            // If not set, fallback to default
            val skipConfirmation = args[options.skipConfirmation] ?: context.config.getUserData(context.loritta, context.user.idLong).quickPunishment
            // The silent option is only useful when punishing users using the "skip confirmation" check
            // Because if not, Loritta will respect the user choice in the ban message
            val isSilent = args[options.isSilent]

            val settings = AdminUtils.retrieveModerationInfo(loritta, context.config)
            val punishmentActions = AdminUtils.retrieveWarnPunishmentActions(loritta, context.config)

            val warnCallback: (suspend (UnleashedContext, Boolean) -> Unit) = { message, isSilent ->
                for (user in users) {
                    val member = context.guild.retrieveMemberOrNull(user)
                    if (!isSilent) {
                        if (settings.sendPunishmentViaDm && context.guild.isMember(user)) {
                            try {
                                val embed = AdminUtils.createPunishmentMessageSentViaDirectMessage(context.guild, context.locale, context.user, context.locale["commands.command.warn.punishAction"], reason)

                                loritta.getOrRetrievePrivateChannelForUser(user).sendMessageEmbeds(embed).queue()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        val punishLogMessage = AdminUtils.getPunishmentForMessage(
                            context.loritta,
                            settings,
                            context.guild,
                            PunishmentAction.WARN
                        )

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
                                    ) + AdminUtils.getStaffCustomTokens(context.user) + AdminUtils.getPunishmentCustomTokens(context.locale, reason, "$LOCALE_PREFIX.warn"),
                                    generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.MemberModerationWarn
                                )

                                textChannel.sendMessage(message).queue()
                            }
                        }
                    }

                    val warnCount = (
                            loritta.newSuspendedTransaction {
                                Warns.selectAll().where { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }.count()
                            } + 1
                            ).toInt()

                    val punishments = punishmentActions.filter { it.warnCount == warnCount }

                    loop@ for (punishment in punishments) {
                        when {
                            punishment.punishmentAction == PunishmentAction.BAN -> BanCommand.ban(loritta, context.i18nContext, settings, context.guild, context.user, context.locale, user, reason, isSilent, 0)
                            member != null && punishment.punishmentAction == PunishmentAction.KICK -> KickCommand.kick(loritta, context.guild, context.i18nContext, context.user, settings, context.locale, user, reason, isSilent)
                            member != null && punishment.punishmentAction == PunishmentAction.MUTE -> {
                                val metadata = punishment.metadata ?: continue@loop
                                val obj = JsonParser.parseString(metadata).obj
                                val time = obj["time"].nullString?.let { TimeUtils.convertToMillisRelativeToNow(it) }
                                MuteCommand.muteUser(context, settings, member, time, context.locale, user, reason, isSilent)
                            }
                        }
                    }

                    loritta.newSuspendedTransaction {
                        Warn.new {
                            this.guildId = context.guild.idLong
                            this.userId = user.idLong
                            this.receivedAt = System.currentTimeMillis()
                            this.punishedById = context.user.idLong
                            this.content = reason
                        }

                        // Log the punishment to the moderation logs
                        loritta.pudding.moderationLogs.logPunishment(
                            context.guild.idLong,
                            user.idLong,
                            context.user.idLong,
                            ModerationLogAction.WARN,
                            reason,
                            null
                        )
                    }
                }

                AdminUtils.sendSuccessfullyPunishedMessage(context, reason)
            }

            if (skipConfirmation) {
                warnCallback.invoke(context, isSilent ?: false)
                return
            }

            AdminUtils.sendConfirmationMessage(context, users, reason, "warn", warnCallback)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val (users, rawReason) = AdminUtils.checkAndRetrieveAllValidUsersFromMessages(context) ?: return null

            val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context, rawReason) ?: return null

            return mapOf(
                options.users to users.joinToString(" ") { it.asMention },
                options.reason to reason,
                options.skipConfirmation to skipConfirmation,
                options.isSilent to silent
            )
        }
    }
}
