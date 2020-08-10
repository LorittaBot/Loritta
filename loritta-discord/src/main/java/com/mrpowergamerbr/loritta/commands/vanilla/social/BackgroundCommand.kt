package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
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
                LorittaReply(
                        "Altere o background e embeleze o seu perfil aqui! <${loritta.instanceConfig.loritta.website.url}user/@me/dashboard/backgrounds>",
                        Emotes.LORI_WOW
                ),
                LorittaReply(
                        "Você pode comprar mais backgrounds para o seu perfil na nossa loja diária! <${loritta.instanceConfig.loritta.website.url}user/@me/dashboard/daily-shop>",
                        Emotes.LORI_WOW,
                        mentionUser = false
                )
		)
	}
}