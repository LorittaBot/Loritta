package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import org.jetbrains.exposed.sql.deleteWhere
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class LorittaUnbanCommand(loritta: LorittaBot) : AbstractCommand(loritta, "lorittaunban", category = net.perfectdreams.loritta.common.commands.CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Desbanir usuários de usar a Loritta"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.size >= 1) {
			val monster = context.args[0].lowercase() // ID
			val profile = loritta.getLorittaProfile(monster)

			if (profile == null) {
				context.reply(
                        LorittaReply(
                                "Usuário não possui perfil na Loritta!",
                                Constants.ERROR
                        )
				)
				return
			}

			loritta.pudding.transaction {
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