package com.mrpowergamerbr.loritta.tables

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.profile.ProfileOptions
import com.mrpowergamerbr.loritta.utils.exposed.jsonb

object Profiles : SnowflakeTable() {
	val xp = long("xp").index()
	val isBanned = bool("banned")
	val bannedReason = text("banned_reason").nullable()
	val lastMessageSentAt = long("last_message_sent_at")
	val lastMessageSentHash = integer("last_message_sent_hash")
	val money = double("money").index()
	var isDonator = bool("donator")
	var donatorPaid = double("donator_paid")
	var donatedAt = long("donated_at")
	var donationExpiresIn = long("donation_expires_in")
	var marriedWith = long("married_with").nullable()
	var marriedAt = long("married_at").nullable()

	val options = jsonb("options", ProfileOptions::class.java, Loritta.GSON)
}