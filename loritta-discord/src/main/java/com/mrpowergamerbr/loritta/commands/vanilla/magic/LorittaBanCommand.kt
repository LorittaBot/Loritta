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
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class LorittaBanCommand : AbstractCommand("lorittaban", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Banir usuários de usar a Loritta"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.size >= 2) {
			val monster = context.getUserAt(0) ?: run {
				context.reply(
                        LorittaReply(
                                "Usuário inválido!",
                                Constants.ERROR
                        )
				)
				return
			}

			val profile = LorittaLauncher.loritta.getLorittaProfile(monster.idLong)

			if (profile == null) {
				context.reply(
                        LorittaReply(
                                "Usuário não possui perfil na Loritta!",
                                Constants.ERROR
                        )
				)
				return
			}

			val reason = context.rawArgs.toMutableList().apply { this.removeAt(0) }.joinToString(" ")

			transaction(Databases.loritta) {
				BannedUsers.insert {
					it[userId] = monster.idLong
					it[bannedAt] = System.currentTimeMillis()
					it[bannedBy] = context.userHandle.idLong
					it[valid] = true
					it[expiresAt] = null
					it[BannedUsers.reason] = reason
				}
			}

			context.sendMessage(context.getAsMention(true) + "Usuário banido com sucesso!")
		} else {
			this.explain(context)
		}
	}
}
