package com.mrpowergamerbr.loritta.utils.networkbans

import com.github.salomonbrys.kotson.fromJson
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.User
import java.io.File

class LorittaNetworkBanManager {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	var networkBannedUsers = mutableListOf<NetworkBanEntry>()

	fun punishUser(user: User, reason: String) {
		val mutualGuilds = lorittaShards.getMutualGuilds(user).filter {
			val member = it.getMember(user) ?: return@filter false
			// Apenas pegue servidores que ela realmente pode banir o infrator
			it.selfMember.hasPermission(Permission.BAN_MEMBERS) && it.selfMember.canInteract(member)
		}

		if (mutualGuilds.isEmpty())
			return

		val serverConfigs = loritta.serversColl.find(
				Filters.and(
						Filters.`in`("_id", mutualGuilds.map { it.id }),
						Filters.eq("moderationConfig.useLorittaBansNetwork", true)
				)
		).toMutableList()

		for (serverConfig in serverConfigs) {
			try {
				val guild = mutualGuilds.firstOrNull { it.id == serverConfig.guildId } ?: continue
				logger.info("Banindo ${user.id} em ${guild.id}...")
				BanCommand.ban(
						serverConfig,
						guild,
						guild.selfMember.user,
						loritta.getLocaleById(serverConfig.localeId),
						user,
						reason,
						false,
						7
				)
			} catch (e: Exception) {
				logger.error(e) { "Erro ao punir o usuário ${user.id} na guild ${serverConfig.guildId}" }
			}
		}
	}

	fun createBanReason(entry: NetworkBanEntry, relayedBan: Boolean): String {
		var reason = entry.reason

		if (relayedBan) {
			reason = "[Loritta's Bans Network] $reason"
		}

		if (entry.guildId != null) {
			val guild = lorittaShards.getGuildById(entry.guildId)

			if (guild != null) {
				reason = "$reason (Punido em ${guild.name.escapeMentions()})"
			}
		}

		return reason
	}

	fun addBanEntry(entry: NetworkBanEntry) {
		val userId = entry.id
		logger.info { "Adicionando $userId na lista de usuários banidos na Loritta Network..." }
		val user = lorittaShards.getUserById(entry.id) ?: run {
			logger.error("$userId não é um usuário válido!")
			return
		}

		if (getNetworkBanEntry(userId) != null) {
			logger.warn("$userId já está banido na Loritta Network!")
			return
		}

		networkBannedUsers.add(entry)

		saveNetworkBannedUsers()

		punishUser(user, createBanReason(entry, true))
	}

	fun getNetworkBanEntry(id: String): NetworkBanEntry? {
		return networkBannedUsers.firstOrNull { it.id == id }
	}

	fun loadNetworkBannedUsers() {
		if (File("./network_banned_users.json").exists()) {
			networkBannedUsers = Loritta.GSON.fromJson(File("./network_banned_users.json").readText())
			networkBannedUsers = networkBannedUsers.distinctBy { it.id }.toMutableList()
			logger.info { "Carregado ${networkBannedUsers.size} usuários banidos da Loritta Network!" }
		}
	}

	fun saveNetworkBannedUsers() {
		File("./network_banned_users.json").writeText(Loritta.GSON.toJson(networkBannedUsers))
	}
}