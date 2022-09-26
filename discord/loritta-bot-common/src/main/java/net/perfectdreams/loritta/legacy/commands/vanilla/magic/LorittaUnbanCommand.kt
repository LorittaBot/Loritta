package net.perfectdreams.loritta.legacy.commands.vanilla.magic

import net.perfectdreams.loritta.legacy.LorittaLauncher
import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.network.Databases
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.common.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.tables.BannedUsers
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class LorittaUnbanCommand : AbstractCommand("lorittaunban", category = net.perfectdreams.loritta.common.commands.CommandCategory.MAGIC, onlyOwner = true) {
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