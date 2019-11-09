package com.mrpowergamerbr.loritta.tables

import net.perfectdreams.loritta.tables.EconomyConfigs
import net.perfectdreams.loritta.tables.LevelConfigs
import org.jetbrains.exposed.sql.ReferenceOption

object ServerConfigs : SnowflakeTable() {
	val donationKey = optReference("donation_key", DonationKeys)
	val donationConfig = optReference("donation_config", DonationConfigs, onDelete = ReferenceOption.CASCADE)
	val birthdayConfig = optReference("birthday_config", BirthdayConfigs, onDelete = ReferenceOption.CASCADE)
	val economyConfig = optReference("economy_config", EconomyConfigs, onDelete = ReferenceOption.CASCADE)
	val levelConfig = optReference("level_config", LevelConfigs, onDelete = ReferenceOption.CASCADE)
}