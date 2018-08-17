package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User

/**
 * Um usuário que está comunicando com a Loritta
 */
open class LorittaUser(val user: User, val config: ServerConfig, val profile: LorittaProfile) {

    val asMention: String
        get() = getAsMention(false)

    fun getAsMention(addSpace: Boolean): String {
        return if (config.mentionOnCommandOutput) user.asMention + (if (addSpace) " " else "") else ""
    }

    open fun hasPermission(lorittaPermission: LorittaPermission): Boolean {
        return false
    }

    /**
     * Verifica se o usuário tem permissão para utilizar um comando
     */
    open fun canUseCommand(context: CommandContext): Boolean {
        return true;
    }
}

/**
 * Um usuário que está comunicando com a Loritta em canais de texto
 */
class GuildLorittaUser(val member: Member, config: ServerConfig, profile: LorittaProfile) : LorittaUser(member.user, config, profile) {

    override fun hasPermission(lorittaPermission: LorittaPermission): Boolean {
        val roles = member.roles.toMutableList()

        val everyone = member.guild.publicRole
        if (everyone != null) {
            roles.add(everyone)
        }

        roles.sortedByDescending { it.position }

		return roles
				.map { config.permissionsConfig.roles.getOrDefault(it.id, PermissionsConfig.PermissionRole()) }
				.any { it.permissions.contains(lorittaPermission) }
    }

    /**
     * Verifica se o usuário tem permissão para utilizar um comando
     */
    override fun canUseCommand(context: CommandContext): Boolean {
        // A coisa mais importante a se verificar é se o comando só pode ser executado pelo dono (para não causar problemas)
        if (context.cmd.onlyOwner && member.user.id != Loritta.config.ownerId) {
            return false;
        }

        // Primeiro iremos verificar as roles
        for (role in member.roles) {
            if (role.name == "Comando: " + context.cmd.label) { // Se o cara tem uma role chamada "Comando: labeldocomando"
                return true;
            }
        }

        // E, finalmente, iremos verificar as permissões do usuário
        if (member.hasPermission(context.event.textChannel, context.cmd.getDiscordPermissions())) {
            return true;
        }

        return false;
    }
}