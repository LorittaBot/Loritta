package com.mrpowergamerbr.loritta.utils.networkbans

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.BlacklistedUsers
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ApplyBansTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		try {
			val bannedUsers = transaction(Databases.loritta) {
				BlacklistedUsers.selectAll().toMutableList()
			}

			logger.info("Verificando ${bannedUsers.size} usuários banidos...")

			for (bannedUser in bannedUsers) {
				val entry = NetworkBanEntry(
						bannedUser[BlacklistedUsers.id].value,
						bannedUser[BlacklistedUsers.guildId],
						bannedUser[BlacklistedUsers.type],
						bannedUser[BlacklistedUsers.reason]
				)

				try {
					val user = lorittaShards.getUserById(entry.id) ?: continue

					loritta.networkBanManager.punishUser(user, loritta.networkBanManager.createBanReason(entry, true))
				} catch (e: Exception) {
					logger.error(e) { "Erro ao processar entry de ${entry.id}!" }
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao processar usuários banidos na network!" }
		}
	}
}