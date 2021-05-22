package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.WebhookUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.commands.CommandCategory

class FaustaoCommand : AbstractCommand("faustão", listOf("faustao"), CommandCategory.FUN) {
	private val frases = listOf(
			"Que isso bicho, ó u cara lá ó",
			"Vamos ver as vídeo cassetadas!",
			"Voltamos já com vídeo cassetadas!",
			"ERRRROOOOOOOOOUUUUUUUU!!!!",
			"E agora, pra desligar essa merda aí, meu. Porra ligou, agora desliga! Tá pegando fogo, bicho!",
			"TÁ PEGANDO FOGO, BICHO!",
			"OLOCO!",
			"Essa fera ai, bicho!",
			"Essa fera ai, meu!",
			"Você destruiu o meu ovo! \uD83C\uDF73",
			"Ih Serjão, sujou! \uD83C\uDFC3\uD83D\uDCA8",
			"ERROU! ⚠",
			"Você vai morrer ⚰",
			"Olha o tamanho da criança",
			"Oito e sete",
			"Ô loco meu!",
			"É brincadera, bicho!",
			"Se vira nos 30!",
			"Quem sabe faz ao vivo!",
			"A TV é chata no domingo, é para quem não tem dinheiro nem o que fazer. Eu trabalho no domingo por isso. O domingo é chato. Para quem pode viajar e passear, o domingo é maravilhoso.",
			"Logo após os reclames do plim-plim!",
			"Olha só o que faz a maldita manguaça, bicho!",
			"{user} é bom tanto no pessoal quanto no profissional.",
			"Essa fera {user} aqui no domingão!")

	private val avatars = listOf(
			"http://i.imgur.com/PS61w6I.png",
			"http://i.imgur.com/ofr6Tkj.png",
			"http://i.imgur.com/nABrbqD.png",
			"http://i.imgur.com/igpGeyg.png",
			"http://i.imgur.com/db2TFRm.png",
			"http://i.imgur.com/RAPYIU9.png",
			"http://i.imgur.com/rVmgwZC.png",
			"http://i.imgur.com/z7Ec5I3.png")

	override fun getDescriptionKey() = LocaleKeyData("commands.command.faustao.description")

	override fun hasCommandFeedback(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val temmie = WebhookUtils.getOrCreateWebhook(context.event.channel, "Faustão")

		val mensagem = frases[Loritta.RANDOM.nextInt(frases.size)].replace("{user}", context.userHandle.asMention)

		context.sendMessage(temmie, WebhookMessageBuilder()
				.setUsername("Faustão")
				.setContent(mensagem)
				.setAvatarUrl(avatars[Loritta.RANDOM.nextInt(avatars.size)])
				.build())
	}
}