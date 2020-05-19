package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.extensions.getOrNull
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.perfectdreams.loritta.dao.servers.moduleconfigs.*
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KMutableProperty1

class ServerConfig(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, ServerConfig>(ServerConfigs)

	val guildId = this.id.value
	var commandPrefix by ServerConfigs.commandPrefix
	var localeId by ServerConfigs.localeId
	var deleteMessageAfterCommand by ServerConfigs.deleteMessageAfterCommand
	var warnOnMissingPermission by ServerConfigs.warnOnMissingPermission
	var warnOnUnknownCommand by ServerConfigs.warnOnUnknownCommand
	var blacklistedChannels by ServerConfigs.blacklistedChannels
	var warnIfBlacklisted by ServerConfigs.warnIfBlacklisted
	var blacklistedWarning by ServerConfigs.blacklistedWarning
	var disabledCommands by ServerConfigs.disabledCommands
	// var donationKey by DonationKey optionalReferencedOn ServerConfigs.donationKey
	var donationConfig by DonationConfig optionalReferencedOn ServerConfigs.donationConfig
	var birthdayConfig by BirthdayConfig optionalReferencedOn ServerConfigs.birthdayConfig
	var economyConfig by EconomyConfig optionalReferencedOn ServerConfigs.economyConfig
	var levelConfig by LevelConfig optionalReferencedOn ServerConfigs.levelConfig
	var starboardConfig by StarboardConfig optionalReferencedOn ServerConfigs.starboardConfig
	var miscellaneousConfig by MiscellaneousConfig optionalReferencedOn ServerConfigs.miscellaneousConfig
	var eventLogConfig by EventLogConfig optionalReferencedOn ServerConfigs.eventLogConfig
	var autoroleConfig by AutoroleConfig optionalReferencedOn ServerConfigs.autoroleConfig
	var inviteBlockerConfig by InviteBlockerConfig optionalReferencedOn ServerConfigs.inviteBlockerConfig
	var welcomerConfig by WelcomerConfig optionalReferencedOn ServerConfigs.welcomerConfig
	var moderationConfig by ModerationConfig optionalReferencedOn ServerConfigs.moderationConfig
	var migrationVersion by ServerConfigs.migrationVersion

	fun getActiveDonationKeys() = transaction(Databases.loritta) {
		DonationKey.find { DonationKeys.activeIn eq this@ServerConfig.id and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()
	}

	fun getActiveDonationKeysValue() = getActiveDonationKeys().sumByDouble { it.value }

	fun getUserData(id: Long): GuildProfile {
		val t = this
		return transaction(Databases.loritta) {
			getUserDataIfExists(id) ?: GuildProfile.new {
				this.guildId = t.guildId
				this.userId = id
				this.money = BigDecimal(0)
				this.quickPunishment = false
				this.xp = 0
				this.isInGuild = true
			}
		}
	}

	fun getUserDataIfExists(id: Long): GuildProfile? {
		return transaction(Databases.loritta) {
			GuildProfile.find { (GuildProfiles.guildId eq guildId) and (GuildProfiles.userId eq id) }.firstOrNull()
		}
	}

	private val cachedData = ConcurrentHashMap<KMutableProperty1<ServerConfig, *>, Optional<Any>>()

	/**
	 * Gets or retrieves from the database the object you've requested
	 */
	fun <T> getCachedOrRetreiveFromDatabase(property: KMutableProperty1<ServerConfig, *>): T {
		if (!cachedData.containsKey(property)) {
			val databaseObject = transaction(Databases.loritta) {
				property.call(this@ServerConfig)
			}
			cachedData[property] = Optional.ofNullable(databaseObject)
			return databaseObject as T
		}
		return cachedData[property]?.getOrNull() as T
	}

	/**
	 * Gets or retrieves from the database the object you've requested
	 */
	suspend fun <T> getCachedOrRetreiveFromDatabaseAsync(loritta: Loritta, property: KMutableProperty1<ServerConfig, *>): T {
		if (!cachedData.containsKey(property)) {
			val databaseObject = loritta.newSuspendedTransaction {
				property.call(this@ServerConfig)
			}
			cachedData[property] = Optional.ofNullable(databaseObject)
			return databaseObject as T
		}
		return cachedData[property]?.getOrNull() as T
	}

	/**
	 * Gets or retrieves from the database the object you've requested
	 */
	suspend fun <T> getCachedOrRetreiveFromDatabaseDeferred(loritta: Loritta, property: KMutableProperty1<ServerConfig, *>): Deferred<T> {
		if (!cachedData.containsKey(property)) {
			val job = loritta.suspendedTransactionAsync {
				val result = property.call(this@ServerConfig)

				cachedData[property] = Optional.ofNullable(result)

				return@suspendedTransactionAsync result
			}
			return job as Deferred<T>
		}
		return GlobalScope.async(loritta.coroutineDispatcher) { cachedData[property]?.getOrNull() as T }
	}
}