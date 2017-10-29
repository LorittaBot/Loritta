package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Role
import java.awt.Color

class MuteCommand : CommandBase() {
	override fun getLabel(): String {
		return "mute"
	}

	override fun getAliases(): List<String> {
		return listOf("mutar", "silenciar")
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["MUTE_DESCRIPTION"]
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("menção/ID" to "ID ou menção do usuário que será silenciado")
	}

	override fun getExample(): List<String> {
		return listOf("@Fulano");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.ADMIN;
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
				var user = LorittaUtils.getUserFromContext(context, 0)

				if (user.id == Loritta.config.clientId) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.get("MUTE_CANT_MUTE_ME"))
					return
				}

				// Vamos pegar se a nossa role existe
				var mutedRoles = context.guild.getRolesByName(context.locale.MUTE_ROLE_NAME, false)
				var mutedRole: Role? = null
				if (mutedRoles.isEmpty()) {
					// Se não existe, vamos criar ela!
					mutedRole = context.guild.controller.createRole()
							.setName(context.locale.MUTE_ROLE_NAME)
							.setColor(Color.BLACK)
							.complete()
				} else {
					// Se existe, vamos carregar a atual
					mutedRole = mutedRoles[0]
				}

				// E agora vamos pegar todos os canais de texto do servidor
				for (textChannel in context.guild.textChannels) {
					var permissionOverride = textChannel.getPermissionOverride(mutedRole)
					if (permissionOverride == null) { // Se é null...
						textChannel.createPermissionOverride(mutedRole)
								.setDeny(Permission.MESSAGE_WRITE) // kk eae men, daora ficar mutado né
								.complete()
					} else {
						if (permissionOverride.denied.contains(Permission.MESSAGE_WRITE)) {
							permissionOverride.manager
									.deny(Permission.MESSAGE_WRITE) // kk eae men, daora ficar mutado né
									.complete()
						}
					}
				}

				// E... finalmente... iremos dar (ou remover) a role para o carinha
				var member = context.guild.getMemberById(user.id)

				if (member.roles.contains(mutedRole)) {
					context.guild.controller.removeRolesFromMember(member, mutedRole).complete()

					context.sendMessage(context.getAsMention(true) + context.locale.MUTE_SUCCESS_OFF.msgFormat(user.id));
				} else {
					context.guild.controller.addRolesToMember(member, mutedRole).complete()

					context.sendMessage(context.getAsMention(true) + context.locale.MUTE_SUCCESS_ON.msgFormat(user.id));
				}
		} else {
			this.explain(context);
		}
	}
}