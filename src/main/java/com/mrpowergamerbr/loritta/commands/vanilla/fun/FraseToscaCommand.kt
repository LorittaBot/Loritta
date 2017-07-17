package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.getOrCreateWebhook
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import java.util.*

class FraseToscaCommand : CommandBase() {
	override fun getLabel(): String {
		return "frasetosca"
	}

	override fun getDescription(): String {
		return "Cria uma frase tosca utilizando várias mensagens recicladas recebidas pela Loritta"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("wow")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun hasCommandFeedback(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
		var text: String
		if (context.args.size >= 1) {
			text = loritta.hal.getSentence(context.args.joinToString(" ").toLowerCase())
		} else {
			text = loritta.hal.sentence
		}
		text = if (text.length > 400) text.substring(0, 400) + "..." else text
		val webhook = getOrCreateWebhook(context.event.textChannel, "Frase Tosca")
		context.sendMessage(webhook, DiscordMessage.builder()
				.username("Gabriela, a amiga da Loritta")
				.content(context.getAsMention(true) + text)
				.avatarUrl("http://i.imgur.com/aATogAg.png")
				.build())
	}
}