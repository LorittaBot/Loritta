package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNull
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData

class EditarXPCommand : AbstractCommand("editxp", listOf("editarxp", "setxp"), category = CommandCategory.SOCIAL) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.editxp.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.editxp.examples")

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_SERVER)
	}

	override fun getUsage() = arguments {
		argument(ArgumentType.USER) {}
		argument(ArgumentType.NUMBER) {}
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val user = context.getUserAt(0)
		if (user != null && context.rawArgs.size == 2) {
			val newXp = context.rawArgs[1].toLongOrNull()

			if (newXp == null) {
				context.sendMessage("${Constants.ERROR} **|** ${context.getAsMention(true)}${context.locale["commands.invalidNumber", context.rawArgs[1]]}")
				return
			}

			if (0 > newXp) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["commands.command.editxp.moreThanZero"])
				return
			}

			// We will only set that the guild is in the guild if the member is *actually* in the guild
			// This fixes a bug that a user could edit XP of users that weren't in the server just to put a dumb badge on their profile
			val userData = context.config.getUserData(user.idLong, context.guild.retrieveMemberOrNull(user) != null)

			loritta.newSuspendedTransaction {
				userData.xp = newXp
			}

			context.sendMessage(context.getAsMention(true) + context.locale["commands.command.editxp.success", user.asMention])
		} else {
			context.explain()
		}
	}
}
