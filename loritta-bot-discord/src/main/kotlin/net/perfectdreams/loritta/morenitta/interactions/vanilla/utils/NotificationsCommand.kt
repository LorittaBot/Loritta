package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.NotificationUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationBuilder
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.GiveawayBuilderScreen
import net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.GiveawayCommand
import net.perfectdreams.loritta.serializable.CorreiosPackageUpdateUserNotification
import net.perfectdreams.loritta.serializable.DailyTaxTaxedUserNotification
import net.perfectdreams.loritta.serializable.DailyTaxWarnUserNotification
import net.perfectdreams.loritta.serializable.UnknownUserNotification
import net.perfectdreams.loritta.serializable.UserId
import java.util.UUID

class NotificationsCommand(val m: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Notifications
    }

    override fun command() = slashCommand(
        name = I18N_PREFIX.Label,
        description = I18N_PREFIX.Description,
        category = CommandCategory.UTILS,
        uniqueId = UUID.fromString("f5e8087d-63e1-40fa-b92d-4f4a9d5af69c")
    ) {
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("notificacoes")
        }

        subcommand(I18N_PREFIX.Configure.Label, I18N_PREFIX.Configure.Description, UUID.fromString("72dc7d63-3875-4c22-9f70-0e6aa79c10a3")) {
            executor = ConfigureNotificationsExecutor(m)
        }
    }

    class ConfigureNotificationsExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val screen = NotificationsSetupScreen.Setup(m)

            context.reply(false, screen.render(context))
        }
    }
}