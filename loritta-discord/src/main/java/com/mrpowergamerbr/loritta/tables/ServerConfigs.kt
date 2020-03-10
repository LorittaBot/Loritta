package com.mrpowergamerbr.loritta.tables

import com.mrpowergamerbr.loritta.utils.exposed.array
import net.perfectdreams.loritta.tables.EconomyConfigs
import net.perfectdreams.loritta.tables.LevelConfigs
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.ReferenceOption

object ServerConfigs : SnowflakeTable() {
	val commandPrefix = text("prefix").default("+")
	val localeId = text("locale_id").default("default")
	val deleteMessageAfterCommand = bool("delete_message_after_command").default(false)
	val warnOnMissingPermission = bool("warn_on_missing_permission").default(false)
	val warnOnUnknownCommand = bool("warn_on_unknown_command").default(false)
	val blacklistedChannels = array<Long>("blacklisted_channels", LongColumnType()).default(arrayOf())
	val warnIfBlacklisted = bool("warn_if_blacklisted").default(false)
	val blacklistedWarning = text("blacklisted_warning").nullable()
	// val donationKey = optReference("donation_key", DonationKeys)
	val donationConfig = optReference("donation_config", DonationConfigs, onDelete = ReferenceOption.CASCADE).index()
	val birthdayConfig = optReference("birthday_config", BirthdayConfigs, onDelete = ReferenceOption.CASCADE).index()
	val economyConfig = optReference("economy_config", EconomyConfigs, onDelete = ReferenceOption.CASCADE).index()
	val levelConfig = optReference("level_config", LevelConfigs, onDelete = ReferenceOption.CASCADE).index()
	val migrationVersion = integer("migration_version").default(0)
}