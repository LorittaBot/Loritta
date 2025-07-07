package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.AdminUtils
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.UnbanCommand.Companion.LOCALE_PREFIX
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.UnbanCommand.Companion.unban
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNull
import java.util.*

class UnbanCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Unban
        val CATEGORY_I18N_PREFIX = I18nKeysData.Commands.Category.Moderation
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("a5a2c779-0b2f-41e7-b129-15a6176149b0")) {
        this.enableLegacyMessageSupport = true
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)
        this.botPermissions = setOf(Permission.BAN_MEMBERS)

        alternativeLegacyLabels.apply {
            add("desbanir")
        }

        executor = BanExecutor(loritta)
    }

    class BanExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
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

            val unbanCallback: suspend (UnleashedContext, Boolean) -> (Unit) = { context, isSilent ->
                for (user in users)
                    unban(loritta, context.i18nContext, settings, context.guild, context.user, context.locale, user, reason, isSilent)

                context.reply(false) {
                    styled(
                        context.locale["$LOCALE_PREFIX.unban.successfullyUnbanned"] + " ${Emotes.LORI_HMPF}",
                        "\uD83C\uDF89"
                    )
                }
            }

            if (skipConfirmation) {
                unbanCallback.invoke(context, isSilent ?: false)
                return
            }

            AdminUtils.sendConfirmationMessage(context, AdminUtils.ConfirmationMessagePunishmentAction.Unban, users, reason, unbanCallback)
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
