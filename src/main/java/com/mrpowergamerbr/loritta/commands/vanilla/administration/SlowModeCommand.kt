package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.Permission

class SlowModeCommand : AbstractCommand("slowmode", listOf("modolento"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["SLOWMODE_Description"]
	}

	override fun getExample(): List<String> {
		return listOf("5");
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val seconds = context.args[0].toIntOrNull()

			if (seconds == null) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["INVALID_NUMBER", context.args[0]])
				return
			}

			if (0 >= seconds) {
				context.config.slowModeChannels.remove(context.event.textChannel!!.id)
				loritta save context.config

				context.sendMessage("\uD83C\uDFC3 **|** " + context.getAsMention(true) + context.locale["SLOWMODE_DisabledInChannel", context.event.textChannel!!.asMention])
				return
			}
			context.config.slowModeChannels[context.event.textChannel!!.id] = seconds
			loritta save context.config

			context.sendMessage("\uD83D\uDC0C **|** " + context.getAsMention(true) + context.locale["SLOWMODE_EnabledInChannel", context.event.textChannel!!.asMention, seconds])
		} else {
			this.explain(context)
		}
	}
}