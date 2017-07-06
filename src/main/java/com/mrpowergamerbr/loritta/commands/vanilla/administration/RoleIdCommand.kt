package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.Permission
import java.util.*

class RoleIdCommand : CommandBase() {
	override fun getLabel(): String {
		return "roleid"
	}

	override fun getDescription(): String {
		return "Pega o ID de um cargo do Discord"
	}

	override fun getUsage(): String {
		return "CargoMencionado"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("@Moderadores")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES);
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
		if (context.message.mentionedRoles.isNotEmpty()) {
			for (r in context.message.mentionedRoles) {
				context.sendMessage(context.getAsMention(true) + r.asMention + " - " + r.id)
			}
		} else {
			context.explain()
		}
	}
}