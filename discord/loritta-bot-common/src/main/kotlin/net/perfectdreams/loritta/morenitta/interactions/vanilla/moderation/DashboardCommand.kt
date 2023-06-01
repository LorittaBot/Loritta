package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.commands.*

class DashboardCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION) {
        executor = DashboardExecutor()
    }

    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Dashboard

        suspend fun executeCompat(context: CommandContextCompat) {
            val guild = context.guildOrNull
            val dashboardUrl = "${context.loritta.config.loritta.website.url}dashboard"
            var url = dashboardUrl

            if (guild != null && guild.selfMember.hasPermission(Permission.MANAGE_SERVER))
                url = "${context.loritta.config.loritta.website.url}guild/${guild.idLong}/configure/"

            context.reply(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.DashboardUrl(url)),
                    Emotes.LoriZap
                )
            }
        }
    }

    inner class DashboardExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            executeCompat(CommandContextCompat.InteractionsCommandContextCompat(context))
        }
    }
}