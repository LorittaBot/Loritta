package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import com.mrpowergamerbr.loritta.utils.LorittaPermission

class DashboardCommand : AbstractCommand("dashboard", listOf("painel", "configurar"), CommandCategory.ADMIN) {
    override fun getDescription(locale: BaseLocale): String {
        return locale.format { commands.moderation.dashboard.description }
    }
    override fun canUseInPrivateChannel(): Boolean {
        return true
    }
    override fun getUsage(locale: BaseLocale): CommandArguments {
        return arguments {
            argument(ArgumentType.USER) {
                optional = false
            }
            argument(ArgumentType.TEXT) {
                optional = false
            }
        }
    }
    override suspend fun run (context: CommandContext, locale: BaseLocale) {
        var guild = context.guild.id.toString()
        var url = "${Loritta.config.websiteUrl}dashboard/configure/{$guild}"

        if (!context.isPrivateChannel) {
            /*
            Se o comando for executado em guildas,
            e o autor tem permissão de alterar configurações no Dashboard (ou tem permissão de Gerenciar servidor),
            dê o url do dashboard diretamente pro servidor.
            */
            if (context.lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)) {
             LoriReply(
                     message = url
             )
            }
            else if (context.guild.selfMember.hasPermission(Permission.MANAGE_SERVER)) {
                LoriReply(
                        message = url
                )
            }
            else {
                // Se o autor não tem nenhuma das permissões, dê a ele o url do dashboard para selecionar o servidor.
                LoriReply(
                        message = "${Loritta.config.websiteUrl}dashboard"
                )
            }
        }
        else {
            // Se o comando for executado em mensagem privada, dê o url do dashboard para selecionar o servidor.
            LoriReply(
                    message = "${Loritta.config.websiteUrl}dashboard"
            )
        }
        }
    }

}