package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.utils.MiscUtil



class LimparCommand : CommandBase() {
	override fun getLabel(): String {
		return "limpar"
	}

	override fun getDescription(): String {
		return "Limpa o chat do canal de texto atual."
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
				context.sendMessage("${LorittaUtils.ERROR} **|** ${context.getAsMention(true)}`${context.args[0]}` é um número inválido!")
				return
			}

			if (toClear !in 2..100) {
				context.sendMessage("${LorittaUtils.ERROR} **|** ${context.getAsMention(true)}Eu só consigo limpar entre 2 até 100 mensagens passadas!")
				return
			}

			val toDelete = mutableListOf<String>()
			var olderThanTwoWeeks = false;
			context.event.textChannel.history.retrievePast(toClear).complete().forEach { msg ->
				val twoWeeksAgo = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000 - MiscUtil.DISCORD_EPOCH shl MiscUtil.TIMESTAMP_OFFSET.toInt()
				if (MiscUtil.parseSnowflake(msg.id) > twoWeeksAgo) {
					olderThanTwoWeeks = true
					return;
				}
				if (context.message.mentionedUsers.isNotEmpty()) {
					if (context.message.mentionedUsers.contains(msg.author)) {
						toDelete.add(msg.id)
					}
				} else {
					toDelete.add(msg.id)
				}
			}

			context.event.textChannel.deleteMessagesByIds(toDelete).complete()

			context.sendMessage("Chat limpo por " + context.handle.asMention + "!" + if (olderThanTwoWeeks) " (Algumas mensagens eram mais velhas que duas semanas e, como o Discord é chato, não consegui deletar elas..." else "")
		} else {
			this.explain(context)
		}
	}
}