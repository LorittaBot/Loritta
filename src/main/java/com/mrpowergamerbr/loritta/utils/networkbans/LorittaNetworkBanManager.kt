package com.mrpowergamerbr.loritta.utils.networkbans

import com.github.salomonbrys.kotson.fromJson
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging
import java.io.File

class LorittaNetworkBanManager {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	private var networkBannedUsers = mutableListOf<NetworkBanEntry>()

	fun addBanEntry(entry: NetworkBanEntry) {
		val userId = entry.id
		val user = lorittaShards.getUserById(entry.id) ?: run {
			logger.error("$userId não é um usuário válido!")
			return
		}

		networkBannedUsers.add(entry)

		saveNetworkBannedUsers()

		val mutualGuilds = lorittaShards.getMutualGuilds(user)

		if (mutualGuilds.isEmpty())
			return

		val serverConfigs = loritta.serversColl.find(
				Filters.and(
						Filters.`in`("_id", mutualGuilds.map { it.id }),
						Filters.eq("moderationConfig.useLorittaBansNetwork")
				)
		).toMutableList()

		for (serverConfig in serverConfigs) {
			val guild = mutualGuilds.firstOrNull { it.id == serverConfig.guildId } ?: continue
			logger.info("Banindo ${entry.id} em ${guild.id}...")
			BanCommand.ban(
					serverConfig,
					guild,
					guild.selfMember.user,
					loritta.getLocaleById(serverConfig.localeId),
					user,
					entry.reason,
					false,
					7
			)
		}
	}

	fun getNetworkBanEntry(id: String): NetworkBanEntry? {
		return networkBannedUsers.firstOrNull { it.id == id }
	}

	fun loadNetworkBannedUsers() {
		if (File("./network_banned_users.json").exists())
			networkBannedUsers = Loritta.GSON.fromJson(File("./network_banned_users.json").readText())
	}

	fun saveNetworkBannedUsers() {
		File("./network_banned_users.json").writeText(Loritta.GSON.toJson(networkBannedUsers))
	}
}