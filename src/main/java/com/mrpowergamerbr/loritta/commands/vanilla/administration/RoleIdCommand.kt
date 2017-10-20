package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.Permission
import java.util.*

class RoleIdCommand : CommandBase() {
	override fun getLabel(): String {
		return "roleid"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.ROLEID_DESCRIPTION.msgFormat()
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
		if (context.rawArgs.isNotEmpty()) {
			val argument = context.rawArgs[0]

			val roles = context.guild.roles.filter { it.name.contains(argument, true) }

			for (role in roles) {
				context.sendMessage(context.getAsMention(true) + role.name + " - " + role.id)
			}
		} else {
			context.explain()
		}
	}
}