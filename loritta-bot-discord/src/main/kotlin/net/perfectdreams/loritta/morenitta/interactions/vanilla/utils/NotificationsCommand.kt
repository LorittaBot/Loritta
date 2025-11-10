package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

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
        this.enableLegacyMessageSupport = true
        this.alternativeLegacyAbsoluteCommandPaths.apply {
            add("notificacoes")
        }

        subcommand(I18N_PREFIX.Configure.Label, I18N_PREFIX.Configure.Description, UUID.fromString("72dc7d63-3875-4c22-9f70-0e6aa79c10a3")) {
            executor = ConfigureNotificationsExecutor(m)
        }
    }

    class ConfigureNotificationsExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val screen = NotificationsSetupScreen.Setup(m)

            context.reply(false, screen.render(context))
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return emptyMap()
        }
    }
}