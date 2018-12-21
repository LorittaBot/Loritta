package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.loritta.LorittaCommand
import net.perfectdreams.commands.loritta.notNull
import kotlin.contracts.ExperimentalContracts

class MagicPingCommand : LorittaCommand(arrayOf("magicping")) {
	@Subcommand
	suspend fun root(context: CommandContext, locale: BaseLocale) {
		context.reply(
				LoriReply(
						"Na verdade isto é só um comando para testes... yay?"
				)
		)
	}

	@ExperimentalContracts
	@Subcommand(["mention"])
	suspend fun mentionUser(context: CommandContext, locale: BaseLocale, user: User?) {
		notNull(user, "that ain't a user dawg")

		context.reply(
				LoriReply(
						"Você mencionou ${user.asMention}!",
						"<:ralsei_surprise:525274650473791489>"
				)
		)
	}
}