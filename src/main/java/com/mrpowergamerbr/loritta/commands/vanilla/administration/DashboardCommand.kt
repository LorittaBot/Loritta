package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import com.mrpowergamerbr.loritta.utils.LorittaPermission
// importar a nova framework e blabla
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.loritta.LorittaCommand
import net.perfectdreams.commands.loritta.LorittaCommandContext

class DashboardCommand : LorittaCommand(arrayOf("dashboard", "painel", "configurar"), CommandCategory.ADMIN) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale.format { commands.moderation.dashboard.description }
    }

    override fun canUseInPrivateChannel(): Boolean {
        return true
    }


    @Subcommand
    suspend fun root (context: LorittaCommandContext, locale: BaseLocale) {

        var guild: String = context.guild.id.toString()
        var url = "${Loritta.config.websiteUrl}dashboard/configure/{$guild}"

        if (!context.isPrivateChannel) {

            /*
            Se o comando for executado em guildas,
            e o autor tem permissão de alterar configurações no Dashboard (ou tem permissão de Gerenciar servidor),
            dê o url do dashboard diretamente pro servidor.
            */

            if (context.lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)) {
                context.sendMessage(url)
            }

            else if (context.guild.selfMember.hasPermission(Permission.MANAGE_SERVER)) {
                context.sendMessage(url)
            }

            else {
                // Se o autor não tem nenhuma das permissões, dê a ele o url do dashboard para selecionar o servidor.

                context.sendMessage("${Loritta.config.websiteUrl}dashboard")
            }

        }
        else {
            // Se o comando for executado em mensagem privada, dê o url do dashboard para selecionar o servidor.

            context.sendMessage("${Loritta.config.websiteUrl}dashboard")
        }

    }
}