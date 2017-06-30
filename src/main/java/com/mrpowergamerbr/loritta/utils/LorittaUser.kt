package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.dv8tion.jda.core.entities.Member

/**
 * Um usuário que está comunicando com a Loritta
 */
class LorittaUser(val member: Member, val config: ServerConfig, val profile: LorittaProfile) {

    val asMention: String
        get() = getAsMention(false)

    fun getAsMention(addSpace: Boolean): String {
        return if (config.mentionOnCommandOutput) member.asMention + (if (addSpace) " " else "") else ""
    }

    /**
     * Verifica se o usuário tem permissão para utilizar um comando
     */
    fun canUseCommand(context: CommandContext): Boolean {
        // A coisa mais importante a se verificar é se o comando só pode ser executado pelo dono (para não causar problemas)
        if (context.cmd.onlyOwner() && member.user.id != Loritta.config.ownerId) {
            return false;
        }

        // Primeiro iremos verificar as roles
        for (role in member.roles) {
            if (role.name.equals("Comando: " + context.cmd.label)) { // Se o cara tem uma role chamada "Comando: labeldocomando"
                return true;
            }
        }

        // E, finalmente, iremos verificar as permissões do usuário
        if (member.hasPermission(context.event.textChannel, context.cmd.discordPermissions)) {
            return true;
        }

        return false;
    }
}