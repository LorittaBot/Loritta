package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.getOrCreateWebhook
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import java.util.*

private val frases = listOf(
	"Que isso bixo, ó u cara lá ó",
	"Vamos ver as vídeos cassetadas",
	"Voltamos já com vídeos cassetadas",
	"ERRRROOOOOOOOOUUUUUUUU!!!!",
	"E agora, pra desligar essa merda aí, meu. Porra ligou, agora desliga! Tá pegando fogo bixo!",
	"Está fera ai bixo",
	"Olha o tamanho da criança",
	"Oito e sete",
	"Ô loco meu!",
	"É brincadera bicho.",
	"Se vira nos 30!",
	"Quem sabe faz ao vivo!",
	"A TV é chata no domingo, é para quem não tem dinheiro nem o que fazer. Eu trabalho no domingo por isso. O domingo é chato. Para quem pode viajar e passear, o domingo é maravilhoso.",
	"Logo após os reclames do plim-plim!",
	"Olha só o que faz a maldita manguaça bicho!",
	"{user} é bom tanto no pessoal como no profissional.")

private val avatars = listOf(
	"http://i.imgur.com/PS61w6I.png",
	"http://i.imgur.com/ofr6Tkj.png",
	"http://i.imgur.com/nABrbqD.png",
	"http://i.imgur.com/igpGeyg.png",
	"http://i.imgur.com/db2TFRm.png",
	"http://i.imgur.com/RAPYIU9.png",
	"http://i.imgur.com/rVmgwZC.png",
	"http://i.imgur.com/z7Ec5I3.png")

class FaustaoCommand : CommandBase() {
	override fun getLabel(): String {
		return "faustão"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun getDescription(): String {
		return "Invoque o querido Faustão no seu servidor!"
	}

	override fun hasCommandFeedback(): Boolean {
		return false
	}

	override fun getAliases(): List<String> {
		return Arrays.asList("faustao")
	}

	override fun run(context: CommandContext) {
		val temmie = getOrCreateWebhook(context.event.textChannel, "Faustão")

		val mensagem = frases[Loritta.random.nextInt(frases.size)].replace("{user}", context.getAsMention(false));

		context.sendMessage(temmie, DiscordMessage.builder()
				.username("Faustão")
				.content(mensagem)
				.avatarUrl(avatars[Loritta.random.nextInt(avatars.size)])
				.build())
	}
}