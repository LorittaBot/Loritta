package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class EditarXPCommand : AbstractCommand("editxp", listOf("editarxp"), category = CommandCategory.SOCIAL) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["commands.social.editxp.description"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getUsage(): String {
		return "usuário quantidade"
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_SERVER)
	}

	override fun getUsage(locale: BaseLocale) = arguments {
		argument(ArgumentType.USER) {}
		argument(ArgumentType.NUMBER) {}
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val user = context.getUserAt(0)
		if (user != null && context.rawArgs.size == 2) {
			val newXp = context.rawArgs[1].toLongOrNull()

			if (newXp == null) {
				context.sendMessage("${Constants.ERROR} **|** ${context.getAsMention(true)}${context.locale["loritta.invalidNumber", context.rawArgs[1]]}")
				return
			}

			if (0 > newXp) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["commands.social.editxp.moreThanZero"])
				return
			}

			val userData = context.config.getUserData(user.idLong)

			loritta.newSuspendedTransaction {
				userData.xp = newXp
			}

			context.sendMessage(context.getAsMention(true) + context.locale["commands.social.editxp.success", user.asMention])
		} else {
			context.explain()
		}
	}
}