package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.loritta.LorittaCommand
import net.perfectdreams.commands.loritta.LorittaCommandContext
import net.perfectdreams.commands.loritta.notNull
import java.awt.image.BufferedImage
import kotlin.contracts.ExperimentalContracts

class MagicPingCommand : LorittaCommand(arrayOf("magicping")) {
	@Subcommand
	suspend fun root(context: LorittaCommandContext, locale: BaseLocale) {
		context.reply(
				LoriReply(
						"Na verdade isto é só um comando para testes... yay?"
				)
		)
	}

	@ExperimentalContracts
	@Subcommand(["mention"])
	suspend fun mentionUser(context: LorittaCommandContext, locale: BaseLocale, user: User?) {
		notNull(user, "that ain't a user dawg")

		context.reply(
				LoriReply(
						"Você mencionou ${user.asMention}!",
						"<:ralsei_surprise:525274650473791489>"
				)
		)
	}

	@ExperimentalContracts
	@Subcommand(["image"])
	suspend fun mentionUser(context: LorittaCommandContext, locale: BaseLocale, image: BufferedImage?) {
		notNull(image, "that ain't a image dawg")

		context.sendFile(image, "owo.png", "Não sei se é verdade, só to repassando")
	}
}