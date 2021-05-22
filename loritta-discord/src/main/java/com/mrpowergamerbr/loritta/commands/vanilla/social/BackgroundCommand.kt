package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.Emotes

class BackgroundCommand : AbstractCommand("background", listOf("papeldeparede"), CommandCategory.SOCIAL) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.background.description")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
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