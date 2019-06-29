package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

/**
 * Um usuário que está comunicando com a Loritta
 */
open class LorittaUser(val user: User, val config: MongoServerConfig, val profile: Profile) {
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
		// A coisa mais importante a se verificar é se o comando só pode ser executado pelo dono (para não causar problemas)
		if (context.cmd.onlyOwner && !loritta.config.isOwner(context.userHandle.id))
			return false

		if (!context.cmd.canHandle(context))
			return false

		return true
	}

	/**
	 * Verifica se o usuário tem permissão para utilizar um comando
	 */
	open fun canUseCommand(context: LorittaCommandContext): Boolean {
		if (context !is DiscordCommandContext)
			throw UnsupportedOperationException("I don't know how to handle a $context yet!")

		// A coisa mais importante a se verificar é se o comando só pode ser executado pelo dono (para não causar problemas)
		if (context.command.onlyOwner && !loritta.config.isOwner(context.userHandle.id)) {
			return false
		}

		return true
	}
}

/**
 * Um usuário que está comunicando com a Loritta em canais de texto
 */
class GuildLorittaUser(val member: Member, config: MongoServerConfig, profile: Profile) : LorittaUser(member.user, config, profile) {
	override fun hasPermission(lorittaPermission: LorittaPermission): Boolean {
		val roles = member.roles.toMutableList()

		roles.add(member.guild.publicRole)

		roles.sortByDescending { it.position }

		if (lorittaPermission == LorittaPermission.IGNORE_COMMANDS) {
			// Caso seja IGNORE_COMMANDS, vamos processar de uma maneira diferente
			for (role in roles) {
				val permissionConfig = config.permissionsConfig.roles.getOrDefault(role.id, PermissionsConfig.PermissionRole())
				if (!permissionConfig.permissions.contains(lorittaPermission))
					return false
			}
		}

		return roles
				.map { config.permissionsConfig.roles.getOrDefault(it.id, PermissionsConfig.PermissionRole()) }
				.any { it.permissions.contains(lorittaPermission) }
	}

	/**
	 * Verifica se o usuário tem permissão para utilizar um comando
	 */
	override fun canUseCommand(context: CommandContext): Boolean {
		if (!super.canUseCommand(context))
			return false

		// E, finalmente, iremos verificar as permissões do usuário
		if (member.hasPermission(context.event.textChannel!!, context.cmd.getDiscordPermissions())) {
			return true
		}

		return false
	}

	/**
	 * Verifica se o usuário tem permissão para utilizar um comando
	 */
	override fun canUseCommand(context: LorittaCommandContext): Boolean {
		if (!super.canUseCommand(context))
			return false

		if (context is DiscordCommandContext && context.command is LorittaDiscordCommand) {
			// E, finalmente, iremos verificar as permissões do usuário
			if (!member.hasPermission(context.event.textChannel!!, context.command.discordPermissions))
				return false
		}

		return true
	}
}