package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.Command
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

class DashboardCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Dashboard
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("398dd3b8-bf0b-436d-ac2d-249316aa4cf7")) {
        enableLegacyMessageSupport = true

        this.alternativeLegacyLabels.apply {
            add("painel")
            add("configurar")
            add("config")
        }

        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        executor = DashboardExecutor()
    }

    inner class DashboardExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val guild = context.guildOrNull
            val dashboardUrl = "${context.loritta.config.loritta.website.url}dashboard"
            var url = dashboardUrl

            if (guild != null && context.member.hasPermission(Permission.MANAGE_SERVER))
                url = "${context.loritta.config.loritta.website.url}guild/${guild.idLong}/configure/"

            context.reply(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.DashboardUrl(url)),
                    Emotes.LoriZap
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = emptyMap()
    }
}