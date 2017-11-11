package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.Permission

class EditarXPCommand : CommandBase("editarxp") {
	override fun getDescription(locale: BaseLocale): String {
		return locale["EDITARXP_DESCRIPTION"]
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.SOCIAL;
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_SERVER)
	}

	override fun run(context: CommandContext) {
		val user = LorittaUtils.getUserFromContext(context, 0)
		if (user != null && context.rawArgs.size == 2) {
			val newXp = context.rawArgs[1].toIntOrNull()

			if (newXp == null) {
				context.sendMessage("${Constants.ERROR} **|** ${context.getAsMention(true)}${context.locale["INVALID_NUMBER", context.rawArgs[1]]}")
				return
			}

			if (0 > newXp) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["EDITARXP_MORE_THAN_ZERO"])
				return
			}

			val userData = context.config.userData.getOrDefault(user.id, LorittaServerUserData())

			userData.xp = newXp

			context.config.userData[user.id] = userData

			loritta save context.config

			context.sendMessage(context.getAsMention(true) + context.locale["EDITARXP_SUCCESS", user.asMention])
		} else {
			context.explain()
		}
	}
}