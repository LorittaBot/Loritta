package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNull
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.LorittaBot

class EditarXPCommand(loritta: LorittaBot) : AbstractCommand(loritta, "editxp", listOf("editarxp", "setxp"), category = net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
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
			val userData = context.config.getUserData(loritta, user.idLong, context.guild.retrieveMemberOrNull(user) != null)

			loritta.newSuspendedTransaction {
				userData.xp = newXp
			}

			context.sendMessage(context.getAsMention(true) + context.locale["commands.command.editxp.success", user.asMention])
		} else {
			context.explain()
		}
	}
}
