package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply

class BackgroundCommand(loritta: LorittaBot) : AbstractCommand(loritta, "background", listOf("papeldeparede"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.background.description")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		context.reply(
                LorittaReply(
                        "Altere o background e embeleze o seu perfil aqui! <${loritta.config.loritta.website.url}dashboard/backgrounds>",
                        Emotes.LORI_WOW
                ),
                LorittaReply(
                        "Você pode comprar mais backgrounds para o seu perfil na nossa loja diária! <${loritta.config.loritta.website.url}user/@me/dashboard/daily-shop>",
                        Emotes.LORI_WOW,
                        mentionUser = false
                )
		)
	}
}