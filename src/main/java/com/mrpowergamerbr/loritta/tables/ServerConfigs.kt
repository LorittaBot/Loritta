package com.mrpowergamerbr.loritta.tables

object ServerConfigs : SnowflakeTable() {
	val donationKey = optReference("donation_key", DonationKeys)
	val donationConfig = optReference("donation_config", DonationConfigs)
}