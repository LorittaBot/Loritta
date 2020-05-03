package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import net.perfectdreams.loritta.dao.EconomyConfig
import net.perfectdreams.loritta.dao.LevelConfig
import net.perfectdreams.loritta.dao.StarboardConfig
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

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
	// var donationKey by DonationKey optionalReferencedOn ServerConfigs.donationKey
	var donationConfig by DonationConfig optionalReferencedOn ServerConfigs.donationConfig
	var birthdayConfig by BirthdayConfig optionalReferencedOn ServerConfigs.birthdayConfig
	var economyConfig by EconomyConfig optionalReferencedOn ServerConfigs.economyConfig
	var levelConfig by LevelConfig optionalReferencedOn ServerConfigs.levelConfig
	var starboardConfig by StarboardConfig optionalReferencedOn ServerConfigs.starboardConfig
	var migrationVersion by ServerConfigs.migrationVersion

	fun getActiveDonationKeys() = transaction(Databases.loritta) {
		DonationKey.find { DonationKeys.activeIn eq id and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()
	}

	fun getActiveDonationKeysValue() = getActiveDonationKeys().sumByDouble { it.value }
}