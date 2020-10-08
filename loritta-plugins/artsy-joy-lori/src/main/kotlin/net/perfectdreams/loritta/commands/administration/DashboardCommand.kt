package net.perfectdreams.loritta.commands.administration

import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.Permission
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

class DashboardCommand : LorittaCommand(arrayOf("dashboard", "painel", "configurar", "config"), CommandCategory.ADMIN) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.moderation.dashboard.description"]
    }

    override fun getExamples(locale: BaseLocale): List<String> {
        return listOf(
                "",
                "\uD83D\uDE45"
        )
    }

    override val canUseInPrivateChannel: Boolean = true

    @Subcommand
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
        val dashboardUrl = "${loritta.instanceConfig.loritta.website.url}dashboard"
        var url = dashboardUrl
        if (!context.isPrivateChannel) {
            url = "${loritta.instanceConfig.loritta.website.url}guild/${context.discordGuild!!.id}/configure/"
        }

        /*
        Se o comando for executado em guildas,
        e o autor tem permissão de alterar configurações no Dashboard (ou tem permissão de Gerenciar servidor),
        dê o url do dashboard diretamente pro servidor.
        */

        if (context.args.getOrNull(0) != "\uD83D\uDE45" && !context.isPrivateChannel && (context.lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD) || context.handle.hasPermission(Permission.MANAGE_SERVER))) {
            context.reply(
                    LorittaReply(
                            "Dashboard: $url",
                            "<:wumplus:388417805126467594>"
                    )
            )
        } else {
            // Se o comando for executando em mensagem privada dê o negócio pra selecionar o servidor
            context.reply(
                    LorittaReply(
                            "Dashboard: $dashboardUrl",
                            "<:wumplus:388417805126467594>"
                    )
            )
        }
    }
}
