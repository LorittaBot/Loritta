package com.mrpowergamerbr.loritta.utils.networkbans

import com.github.salomonbrys.kotson.fromJson
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
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
import java.io.File

class LorittaNetworkBanManager {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	var notVerifiedEntries = mutableListOf<NetworkBanEntry>()

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
				if (!guild.isMember(user))
					continue

				logger.info("Banindo ${user.id} em ${guild.id}...")
				BanCommand.ban(
						serverConfig,
						guild,
						guild.selfMember.user,
						loritta.getLegacyLocaleById(serverConfig.localeId),
						user,
						reason,
						false,
						0
				)
			} catch (e: Exception) {
				logger.error(e) { "Erro ao punir o usuário ${user.id} na guild ${serverConfig.guildId}" }
			}
		}
	}

	fun punishUser(user: User, reason: String, guild: Guild) {
		if (!guild.isMember(user)) // Não é um membro da guild atual!
			return

		val member = guild.getMember(user)

		if (guild.selfMember.hasPermission(Permission.BAN_MEMBERS) && guild.selfMember.canInteract(member!!))
			return

		val serverConfig = loritta.getServerConfigForGuild(guild.id)

		try {
			logger.info("Banindo ${user.id} em ${guild.id}...")
			BanCommand.ban(
					serverConfig,
					guild,
					guild.selfMember.user,
					loritta.getLegacyLocaleById(serverConfig.localeId),
					user,
					reason,
					false,
					7
			)
		} catch (e: Exception) {
			logger.error(e) { "Erro ao punir o usuário ${user.id} na guild ${serverConfig.guildId}" }
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
			}
		}

		punishUser(user, createBanReason(entry, true))
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

	fun migrateNetworkBannedUsers() {
		if (File("./network_banned_users.json").exists()) {
			logger.info { "Migrating user bans to the database..." }

			var networkBannedUsers = Loritta.GSON.fromJson<List<NetworkBanEntry>>(File("./network_banned_users.json").readText())
			networkBannedUsers = networkBannedUsers.distinctBy { it.id }.toMutableList()

			for (networkBannedUser in networkBannedUsers) {
				transaction(Databases.loritta) {
					try {
						BlacklistedUsers.insert {
							it[id] = EntityID(networkBannedUser.id.toLong(), BlacklistedUsers)
							it[bannedAt] = System.currentTimeMillis()
							it[guildId] = networkBannedUser.guildId?.toLong()
							it[type] = networkBannedUser.type
							it[reason] = networkBannedUser.reason
							it[globally] = false
						}
					} catch (e: Exception) {
						logger.warn { "Erro ao migrar ban $networkBannedUser "}
					}
				}
			}

			File("./network_banned_users.json").delete()
		}
	}

	fun checkIfUserShouldBeBanned(user: User, guild: Guild, serverConfig: MongoServerConfig): Boolean {
		val globallyBannedEntry = loritta.networkBanManager.getGlobalBanEntry(user.idLong) // oof¹
		if (globallyBannedEntry != null) {
			BanCommand.ban(
					serverConfig,
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
		if (serverConfig.moderationConfig.useLorittaBansNetwork && networkBannedEntry != null) {
			BanCommand.ban(
					serverConfig,
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