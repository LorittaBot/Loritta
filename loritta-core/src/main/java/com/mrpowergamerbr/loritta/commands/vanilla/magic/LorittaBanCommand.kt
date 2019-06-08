package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import org.jetbrains.exposed.sql.transactions.transaction

class LorittaBanCommand : AbstractCommand("lorittaban", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return "Banir usuários de usar a Loritta"
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.size >= 2) {
			val monster = context.args[0].toLowerCase() // ID
			context.args[0] = ""
			val reason = context.args.joinToString(" ")
			val profile = LorittaLauncher.loritta.getLorittaProfile(monster)

			if (profile == null) {
				context.reply(
						LoriReply(
								"Usuário não possui perfil na Loritta!",
								Constants.ERROR
						)
				)
				return
			}

			transaction(Databases.loritta) {
				profile.isBanned = true
				profile.bannedReason = reason
			}

			context.sendMessage(context.getAsMention(true) + "Usuário banido com sucesso!")
		} else {
			this.explain(context)
		}
	}
}