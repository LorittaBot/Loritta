package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
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

	override fun getDescription(): String {
		return "Silencia um usuário por um período de tempo determinado"
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
		return listOf(Permission.BAN_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			try {
				var id = context.args[0];

				if (context.rawArgs[0].startsWith("<") && context.message.mentionedUsers.isNotEmpty()) {
					id = context.message.mentionedUsers[0].id
				}

				// Vamos pegar se a nossa role existe
				var mutedRoles = context.guild.getRolesByName("Silenciado", false)
				var mutedRole: Role? = null
				if (mutedRoles.isEmpty()) {
					// Se não existe, vamos criar ela!
					mutedRole = context.guild.controller.createRole()
							.setName("Silenciado")
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
				var member = context.guild.getMemberById(id)

				if (member.roles.contains(mutedRole)) {
					context.guild.controller.removeRolesFromMember(member, mutedRole).complete()

					context.sendMessage(context.getAsMention(true) + "Usuário `$id` magicamente aprendeu a falar de novo!");
				} else {
					context.guild.controller.addRolesToMember(member, mutedRole).complete()

					context.sendMessage(context.getAsMention(true) + "Usuário `$id` foi silenciado com sucesso!");
				}
			} catch (e: Exception) {
				context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + " Não tenho permissão para softbanir este usuário!");
			}
		} else {
			this.explain(context);
		}
	}
}