package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.getOrCreateWebhook
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import java.util.*

private val responses = Arrays.asList(
		"Vai incomodar outra pessoa, obrigado.",
		"Não sei, mas eu sei que eu moro lá no Cambuci.",
		"Do jeito que eu vejo, sim.",
		"Hmmmm... 🤔",
		"Não posso falar sobre isso.",
		"Não.",
		"Sim.",
		"Eu responderia, mas não quero ferir seus sentimentos.",
		"Provavelmente sim",
		"Provavelmente não",
		"Minhas fontes dizem que sim",
		"Minhas fontes dizem que não",
		"Você pode acreditar nisso",
		"Minha resposta é não",
		"Minha resposta é sim",
		"Do jeito que eu vejo, não.",
		"Melhor não falar isto para você agora...",
		"Sim, com certeza!",
		"Também queria saber...",
		"A minha resposta não importa, o que importa é você seguir o seu coração. 😘",
		"Talvez...",
		"Acho que sim.",
		"Acho que não.",
		"Talvez sim.",
		"Talvez não.",
		"Sim!",
		"Não!",
		"¯\\_(ツ)_/¯")

class MagicBallCommand : CommandBase() {
	override fun getLabel(): String {
		return "vieirinha"
	}

	override fun getDescription(): String {
		return "Pergunte algo para o Vieirinha"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("você me ama?")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun hasCommandFeedback(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val temmie = getOrCreateWebhook(context.event.textChannel, "Vieirinha")

			context.sendMessage(temmie, DiscordMessage.builder()
					.username("Vieirinha")
					.content(context.getAsMention(true) + responses[Loritta.random.nextInt(responses.size)])
					.avatarUrl("http://i.imgur.com/rRtHdti.png")
					.build())
		} else {
			context.explain()
		}
	}
}
