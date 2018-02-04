package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.remove
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.User

class TempRoleCommand : AbstractCommand("temprole", listOf("cargotemp"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["TEMPROLE_Description"]
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES)
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			if (context.rawArgs[0] == "add") {
				val userNames = context.rawArgs
				userNames.remove(0)

				val users = mutableListOf<User>()
			}
		} else {
			this.explain(context)
		}
	}
}