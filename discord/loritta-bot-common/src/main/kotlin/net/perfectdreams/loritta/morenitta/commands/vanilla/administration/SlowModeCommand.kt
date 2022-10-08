package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.LorittaBot
import kotlin.time.Duration.Companion.seconds

class SlowModeCommand(loritta: LorittaBot) : AbstractCommand(loritta, "slowmode", listOf("modolento"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
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
		return listOf(Permission.ManageMessages, Permission.ManageChannels)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.ManageMessages, Permission.ManageChannels)
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

			if (!context.guild.selfMemberHasPermission(Permission.ManageChannels)) {
				// TODO: Colocar uma mensagem melhor
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["commands.invalidNumber", context.args[0]])
				return
			}

			if (0 >= seconds) {
				context.message.textChannel.modifyTextChannel {
					this.rateLimitPerUser = null
				}

				context.sendMessage("\uD83C\uDFC3 **|** " + context.getAsMention(true) + context.locale["commands.command.slowmode.disabledInChannel", context.event.channel.asMention])
				return
			} else if (seconds in 1..21600) {
				context.message.textChannel.modifyTextChannel {
					this.rateLimitPerUser = seconds.seconds
				}

				context.sendMessage("\uD83D\uDC0C **|** " + context.getAsMention(true) + context.locale["commands.command.slowmode.enabledInChannel", context.event.channel.asMention, seconds])
			} else {
				// TODO: Colocar uma mensagem melhor
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["commands.invalidNumber", context.args[0]])
				return
			}
		} else {
			this.explain(context)
		}
	}
}