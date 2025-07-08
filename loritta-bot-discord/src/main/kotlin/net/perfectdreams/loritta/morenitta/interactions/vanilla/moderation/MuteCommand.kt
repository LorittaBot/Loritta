package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.AdminUtils
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.MuteCommand.Companion.muteUser
import net.perfectdreams.loritta.morenitta.interactions.InteractionMessage
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.utils.TimeUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNull
import net.perfectdreams.loritta.morenitta.utils.onResponseByAuthor
import java.time.Instant
import java.util.*

class MuteCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Mute
        val CATEGORY_I18N_PREFIX = I18nKeysData.Commands.Category.Moderation
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("a0930e23-a1e4-4387-83ac-630ccdae33a8")) {
        this.enableLegacyMessageSupport = true
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
        this.botPermissions = setOf(Permission.MODERATE_MEMBERS)

        alternativeLegacyLabels.apply {
            add("mutar")
            add("silenciar")
        }

        executor = MuteExecutor(loritta)
    }

    class MuteExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            // May be multiple in the same string
            val users = string("users", CATEGORY_I18N_PREFIX.Options.Users.Text)
            val time = optionalString("time", I18N_PREFIX.Options.Time.Text)
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

            suspend fun handlePreMute(timeAsDurationMillis: Long?) {
                val muteCallback: suspend (UnleashedContext, Boolean) -> (Unit) = { context, isSilent ->
                    var success = 0

                    for (user in users) {
                        val result = muteUser(context, settings, timeAsDurationMillis, context.locale, user, reason, isSilent)

                        if (!result)
                            continue

                        success++
                    }

                    if (success != 0)
                        AdminUtils.sendSuccessfullyPunishedMessage(context, reason)
                }

                if (skipConfirmation) {
                    muteCallback.invoke(context, isSilent ?: false)
                    return
                }

                AdminUtils.sendConfirmationMessage(context, AdminUtils.ConfirmationMessagePunishmentAction.Mute(timeAsDurationMillis?.let { Instant.ofEpochMilli(it) }), users, reason, muteCallback)
            }

            if (context is LegacyMessageCommandContext) {
                // If this is a legacy message command, we'll do this the old-fashioned way, because supporting the time arguments in a clean way would be painful in a message command
                val message = context.reply(false) {
                    styled(
                        context.locale["commands.category.moderation.setPunishmentTime"],
                        "⏰"
                    )

                    actionRow(
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            ButtonStyle.SECONDARY,
                            context.i18nContext.get(I18N_PREFIX.SetPermanent),
                            {
                                this.emoji = Emoji.fromUnicode("\uD83D\uDD04")
                            }
                        ) { context ->
                            context.deferEdit()
                            loritta.messageInteractionCache.remove(context.event.message.idLong)
                            context.event.message.delete().queue()

                            handlePreMute(null)
                        }
                    )
                } as InteractionMessage.FollowUpInteractionMessage // This is not a REAL interaction message

                val discordMessage = message.message

                discordMessage.onResponseByAuthor(context) { event ->
                    loritta.messageInteractionCache.remove(discordMessage.idLong)
                    discordMessage.delete().queue()

                    val time = TimeUtils.convertToMillisRelativeToNow(event.message.contentDisplay)
                    handlePreMute(time)
                }
                return
            }

            handlePreMute(args[options.time]?.let { TimeUtils.convertToMillisRelativeToNow(it) })
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
