package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation

import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations.DashboardCommand

class DashboardExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val dashboardUrl = "${loritta.config.loritta.website}dashboard"
        var url = dashboardUrl

        // TODO: Get permissions from the interactions itself when Kord implements support for it
        if (context is GuildApplicationCommandContext && context.member.getPermissions().contains(Permission.ManageGuild))
            url = "${loritta.config.loritta.website}guild/${context.guildId}/configure/"

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(DashboardCommand.I18N_PREFIX.DashboardUrl(url)),
                Emotes.LoriZap
            )
        }
    }
}