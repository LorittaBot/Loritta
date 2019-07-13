package net.perfectdreams.loritta.premium.tables

object ServerConfigs : SnowflakeTable() {
	val donationKey = optReference("donation_key", DonationKeys)
}