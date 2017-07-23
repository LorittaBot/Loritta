package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.Permission


class LimparCommand : CommandBase() {
	override fun getLabel(): String {
		return "limpar"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.LIMPAR_DESCRIPTION.msgFormat()
	}

	override fun getUsage(): String {
		return "QuantasMensagens"
	}

	override fun getExample(): List<String> {
		return listOf("10", "25", "7 @Tsugami", "50 @Tsugami @Tsumari")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.ADMIN
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE)
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val toClear = context.args[0].toIntOrNull()

			if (toClear == null) {
				context.sendMessage("${LorittaUtils.ERROR} **|** ${context.getAsMention(true)}${context.locale.INVALID_NUMBER.msgFormat(context.args[0])}")
				return
			}

			if (toClear !in 2..100) {
				context.sendMessage("${LorittaUtils.ERROR} **|** ${context.getAsMention(true)}${context.locale.LIMPAR_INVALID_RANGE.msgFormat()}")
				return
			}

			val toDelete = mutableListOf<String>()
			context.event.textChannel.history.retrievePast(toClear).complete().forEach { msg ->
				if (context.message.mentionedUsers.isNotEmpty()) {
					if (context.message.mentionedUsers.contains(msg.author)) {
						toDelete.add(msg.id)
					}
				} else {
					toDelete.add(msg.id)
				}
			}

			context.event.textChannel.deleteMessagesByIds(toDelete).complete()

			context.sendMessage(context.locale.LIMPAR_SUCCESS.msgFormat(context.asMention))
		} else {
			this.explain(context)
		}
	}
}