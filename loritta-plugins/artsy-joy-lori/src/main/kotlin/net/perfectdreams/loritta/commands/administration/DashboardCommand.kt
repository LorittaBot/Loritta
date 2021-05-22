package net.perfectdreams.loritta.commands.administration

import com.mrpowergamerbr.loritta.utils.LorittaPermission
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase

class DashboardCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("dashboard", "painel", "configurar", "config"), CommandCategory.MODERATION) {
    override fun command() = create {
        localizedDescription("commands.command.dashboard.description")
        localizedExamples("commands.command.dashboard.examples")

        arguments {
            argument(ArgumentType.TEXT) {
                optional = true
            }
        }

        executesDiscord {
            val dashboardUrl = "${loritta.instanceConfig.loritta.website.url}dashboard"
            var url = dashboardUrl

            if (!isPrivateChannel) {
                url = "${loritta.instanceConfig.loritta.website.url}guild/${guild.id}/configure/"
            }

            /*
            Se o comando for executado em guildas,
            e o autor tem permissão de alterar configurações no Dashboard (ou tem permissão de Gerenciar servidor),
            dê o url do dashboard diretamente pro servidor.
            */

            if (args.getOrNull(0) != "\uD83D\uDE45" && !isPrivateChannel && (lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD) || member?.hasPermission(Permission.MANAGE_SERVER) == true)) {
                reply(
                        LorittaReply(
                                "Dashboard: $url",
                                "<:wumplus:388417805126467594>"
                        )
                )
            } else {
                // Se o comando for executando em mensagem privada dê o negócio pra selecionar o servidor
                reply(
                        LorittaReply(
                                "Dashboard: $dashboardUrl",
                                "<:wumplus:388417805126467594>"
                        )
                )
            }
        }
    }
}
