package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes

class BackgroundCommand : AbstractCommand("background", listOf("papeldeparede"), CommandCategory.SOCIAL) {
	override fun getUsage(): String {
		return "<novo background>"
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["BACKGROUND_DESCRIPTION"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		context.reply(
				LoriReply(
						"Devido ao grande número de pessoas enviando backgrounds inapropriados e o espaço imenso necessário para hospedar vários backgrounds, não é mais possível enviar seus próprios backgrounds.",
						Emotes.LORI_RAGE
				),
				LoriReply(
						"Foi substituido um sistema de compra de backgrounds, você pode comprar e alterar backgrounds pelo website! <${loritta.instanceConfig.loritta.website.url}user/@me/dashboard/backgrounds>",
						Emotes.LORI_WOW,
						mentionUser = false
				),
				LoriReply(
						"Se você tem imagens que você acha que seriam legais como background, dê uma passadinha no meu servidor de suporte! Tem um canal de \"backgrounds\" para sugerirem imagens. <${loritta.instanceConfig.loritta.website.url}support>",
						Emotes.LORI_OWO,
						mentionUser = false
				),
				LoriReply(
						"Desculpe pela inconveniência...",
						Emotes.LORI_CRYING,
						mentionUser = false
				)
		)
	}
}