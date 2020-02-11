package com.mrpowergamerbr.loritta.utils.networkbans

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.tables.BlacklistedUsers
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class LorittaNetworkBanManager {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	var notVerifiedEntries = mutableListOf<NetworkBanEntry>()

	fun punishUser(user: User, reason: String, isGlobal: Boolean) {
		val mutualGuilds = lorittaShards.getMutualGuilds(user).filter {
			val member = it.getMember(user) ?: return@filter false
			// Apenas pegue servidores que ela realmente pode banir o infrator
			it.selfMember.hasPermission(Permission.BAN_MEMBERS) && it.selfMember.canInteract(member)
		}

		if (mutualGuilds.isEmpty())
			return

		val serverConfigs = if (isGlobal) {
			loritta.serversColl.find(
					Filters.`in`("_id", mutualGuilds.map { it.id })
			).toMutableList()
		} else {
			loritta.serversColl.find(
					Filters.and(
							Filters.`in`("_id", mutualGuilds.map { it.id }),
							Filters.eq("moderationConfig.useLorittaBansNetwork", true)
					)
			).toMutableList()
		}

		for (guild in mutualGuilds) {
			try {
				if (!guild.isMember(user))
					continue

				val legacyServerConfig = serverConfigs.firstOrNull { it.guildId == guild.id } ?: continue
				val serverConfig = loritta.getOrCreateServerConfig(guild.idLong)

				logger.info("Banindo ${user.id} em ${guild.id}...")
				BanCommand.ban(
						legacyServerConfig,
						guild,
						guild.selfMember.user,
						loritta.getLegacyLocaleById(serverConfig.localeId),
						user,
						reason,
						false,
						0
				)
			} catch (e: Exception) {
				logger.error(e) { "Error while punishing user ${user.id} in guild ${guild.idLong}" }
			}
		}
	}

	fun punishUser(user: User, reason: String, guild: Guild) {
		if (!guild.isMember(user)) // Não é um membro da guild atual!
			return

		val member = guild.getMember(user)

		if (guild.selfMember.hasPermission(Permission.BAN_MEMBERS) && guild.selfMember.canInteract(member!!))
			return

		val serverConfig = loritta.getOrCreateServerConfig(guild.idLong)
		val legacyServerConfig = loritta.getServerConfigForGuild(guild.id)

		try {
			logger.info("Banindo ${user.id} em ${guild.id}...")
			BanCommand.ban(
					legacyServerConfig,
					guild,
					guild.selfMember.user,
					loritta.getLegacyLocaleById(serverConfig.localeId),
					user,
					reason,
					false,
					7
			)
		} catch (e: Exception) {
			logger.error(e) { "Erro ao punir o usuário ${user.id} na guild ${legacyServerConfig.guildId}" }
		}
	}

	fun createBanReason(entry: NetworkBanEntry, relayedBan: Boolean): String {
		var reason = entry.reason

		reason = "[Loritta's Bans Network] $reason"

		if (entry.guildId != null) {
			val guild = lorittaShards.getGuildById(entry.guildId)

			if (guild != null) {
				reason = "$reason (Encontrado em ${guild.name.escapeMentions()})"
			}
		}

		return reason
	}

	suspend fun addNonVerifiedEntry(entry: NetworkBanEntry) {
		val userId = entry.id
		logger.info { "Adicionando $userId na lista de usuários não verificados para serem banidos na Loritta Network..." }
		val user = lorittaShards.retrieveUserById(entry.id) ?: run {
			logger.error("$userId não é um usuário válido!")
			return
		}

		if (getNetworkBanEntry(userId) != null) {
			logger.warn("$userId já está banido na Loritta Network!")
			return
		}

		if (getNonVerifiedBanEntry(userId) != null) {
			logger.warn("$userId já está na lista de usuários não verificados da Loritta Network!")
			return
		}

		notVerifiedEntries.add(entry)
	}

	suspend fun addBanEntry(entry: NetworkBanEntry) {
		val userId = entry.id
		logger.info { "Adicionando $userId na lista de usuários banidos na Loritta Network..." }
		val user = lorittaShards.retrieveUserById(entry.id) ?: run {
			logger.error("$userId não é um usuário válido!")
			return
		}

		if (getNetworkBanEntry(userId) != null) {
			logger.warn("$userId já está banido na Loritta Network!")
			return
		}

		transaction(Databases.loritta) {
			BlacklistedUsers.insert {
				it[BlacklistedUsers.id] = EntityID(entry.id, BlacklistedUsers)
				it[BlacklistedUsers.guildId] = entry.guildId
				it[BlacklistedUsers.reason] = entry.reason
				it[BlacklistedUsers.bannedAt] = System.currentTimeMillis()
				it[BlacklistedUsers.globally] = false
				it[BlacklistedUsers.type] = entry.type
			}
		}

		punishUser(user, createBanReason(entry, true), false)

		lorittaShards.queryAllLorittaClusters("/api/v1/loritta/global-bans/sync/$userId")
	}

	fun getGlobalBanEntry(id: Long): NetworkBanEntry? {
		val result = transaction(Databases.loritta) {
			BlacklistedUsers.select {
				BlacklistedUsers.id eq id and (BlacklistedUsers.globally eq true)
			}.firstOrNull()
		} ?: return null

		return NetworkBanEntry(
				result[BlacklistedUsers.id].value,
				result[BlacklistedUsers.guildId],
				result[BlacklistedUsers.type],
				result[BlacklistedUsers.reason]
		)
	}

	fun getNetworkBanEntry(id: Long): NetworkBanEntry? {
		val result = transaction(Databases.loritta) {
			BlacklistedUsers.select {
				BlacklistedUsers.id eq id
			}.firstOrNull()
		} ?: return null

		return NetworkBanEntry(
				result[BlacklistedUsers.id].value,
				result[BlacklistedUsers.guildId],
				result[BlacklistedUsers.type],
				result[BlacklistedUsers.reason]
		)
	}

	fun getNonVerifiedBanEntry(id: Long): NetworkBanEntry? {
		return notVerifiedEntries.firstOrNull { it.id == id }
	}

	fun checkIfUserShouldBeBanned(user: User, guild: Guild, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig): Boolean {
		val globallyBannedEntry = loritta.networkBanManager.getGlobalBanEntry(user.idLong) // oof¹
		if (globallyBannedEntry != null) {
			BanCommand.ban(
					legacyServerConfig,
					guild,
					guild.selfMember.user,
					com.mrpowergamerbr.loritta.utils.loritta.getLegacyLocaleById(serverConfig.localeId),
					user,
					"[Loritta's Global Bans] ${globallyBannedEntry.reason}",
					false,
					0
			)
			return true
		}

		val networkBannedEntry = loritta.networkBanManager.getNetworkBanEntry(user.idLong) // oof²
		if (legacyServerConfig.moderationConfig.useLorittaBansNetwork && networkBannedEntry != null) {
			BanCommand.ban(
					legacyServerConfig,
					guild,
					guild.selfMember.user,
					com.mrpowergamerbr.loritta.utils.loritta.getLegacyLocaleById(serverConfig.localeId),
					user,
					"[Loritta's Bans Network] ${networkBannedEntry.reason}",
					false,
					0
			)
			return true
		}

		return false
	}
}