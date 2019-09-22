package com.mrpowergamerbr.loritta.tables

import net.perfectdreams.loritta.tables.EconomyConfigs

object ServerConfigs : SnowflakeTable() {
	val donationKey = optReference("donation_key", DonationKeys)
	val donationConfig = optReference("donation_config", DonationConfigs)
	val birthdayConfig = optReference("birthday_config", BirthdayConfigs)
	val economyConfig = optReference("economy_config", EconomyConfigs)
}