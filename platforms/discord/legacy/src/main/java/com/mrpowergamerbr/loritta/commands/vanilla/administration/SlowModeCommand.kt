package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class SlowModeCommand : AbstractCommand("slowmode", listOf("modolento"), CommandCategory.MODERATION) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.slowmode.description")

	override fun getUsage(): CommandArguments {
		return arguments {
			argument(ArgumentType.NUMBER) {
				optional = false
			}
		}
	}

	override fun getExamplesKey() = LocaleKeyData("commands.command.slowmode.examples")

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
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["commands.invalidNumber", context.args[0]])
				return
			}

			if (0 >= seconds) {
				if (context.guild.selfMember.hasPermission(Permission.MANAGE_CHANNEL))
					context.message.textChannel.manager.setSlowmode(0).queue()

				context.sendMessage("\uD83C\uDFC3 **|** " + context.getAsMention(true) + context.locale["commands.command.slowmode.disabledInChannel", context.event.textChannel!!.asMention])
				return
			}

			if (seconds in 0..21600 && context.guild.selfMember.hasPermission(Permission.MANAGE_CHANNEL)) // 6 horas
				context.message.textChannel.manager.setSlowmode(seconds).queue()
			else {
				// TODO: Colocar uma mensagem melhor
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["commands.invalidNumber", context.args[0]])
				return
			}

			context.sendMessage("\uD83D\uDC0C **|** " + context.getAsMention(true) + context.locale["commands.command.slowmode.enabledInChannel", context.event.textChannel!!.asMention, seconds])
		} else {
			this.explain(context)
		}
	}
}