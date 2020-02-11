package com.mrpowergamerbr.loritta.tables

object Profiles : SnowflakeTable() {
	val xp = long("xp").index()
	val isBanned = bool("banned")
	val bannedReason = text("banned_reason").nullable()
	val lastMessageSentAt = long("last_message_sent_at")
	val lastMessageSentHash = integer("last_message_sent_hash")
	val lastCommandSentAt = long("last_command_sent_at").nullable()
	val money = double("money").index()
	var isDonator = bool("donator")
	var donatorPaid = double("donator_paid")
	var donatedAt = long("donated_at")
	var donationExpiresIn = long("donation_expires_in")
	var isAfk = bool("isAfk")
	var afkReason = text("afkReason").nullable()
	var settings = reference("settings", UserSettings)
	var marriage = reference("marriage", Marriages).nullable()
}