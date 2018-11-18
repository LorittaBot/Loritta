package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.Permission

class SlowModeCommand : AbstractCommand("slowmode", listOf("modolento"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["SLOWMODE_Description"]
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.NUMBER) {
				optional = false
			}
		}
	}

	override fun getExamples(): List<String> {
		return listOf("5")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE, Permission.MANAGE_CHANNEL)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE, Permission.MANAGE_CHANNEL)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val seconds = context.args[0].toIntOrNull()

			if (seconds == null) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["INVALID_NUMBER", context.args[0]])
				return
			}

			if (0 >= seconds) {
				context.config.slowModeChannels.remove(context.event.textChannel!!.id)
				if (context.guild.selfMember.hasPermission(Permission.MANAGE_CHANNEL))
					context.message.textChannel.manager.setSlowmode(0).queue()

				loritta save context.config

				context.sendMessage("\uD83C\uDFC3 **|** " + context.getAsMention(true) + context.locale["SLOWMODE_DisabledInChannel", context.event.textChannel!!.asMention])
				return
			}

			context.config.slowModeChannels[context.event.textChannel!!.id] = seconds
			if (seconds in 0..120 && context.guild.selfMember.hasPermission(Permission.MANAGE_CHANNEL))
				context.message.textChannel.manager.setSlowmode(seconds).queue()

			loritta save context.config

			context.sendMessage("\uD83D\uDC0C **|** " + context.getAsMention(true) + context.locale["SLOWMODE_EnabledInChannel", context.event.textChannel!!.asMention, seconds])
		} else {
			this.explain(context)
		}
	}
}