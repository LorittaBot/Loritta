package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.tables.BannedUsers
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class LorittaUnbanCommand : AbstractCommand("lorittaunban", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Desbanir usuários de usar a Loritta"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.size >= 1) {
			val monster = context.args[0].toLowerCase() // ID
			val profile = LorittaLauncher.loritta.getLorittaProfile(monster)

			if (profile == null) {
				context.reply(
                        LorittaReply(
                                "Usuário não possui perfil na Loritta!",
                                Constants.ERROR
                        )
				)
				return
			}

			transaction(Databases.loritta) {
				BannedUsers.deleteWhere {
					BannedUsers.userId eq profile.userId
				}
			}

			context.sendMessage(context.getAsMention(true) + "Usuário desbanido com sucesso!")
		} else {
			this.explain(context)
		}
	}
}