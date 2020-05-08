package net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object MessageProofs : LongIdTable() {
	val reportId = reference("report", UserReports).index()

	val messageId = long("message")
	val channelId = long("channel")
	val guildId = long("guild")

	val authorId = long("author")
	val authorName = text("author_name")
	val authorDiscriminator = text("author_discriminator")

	val content = text("content")
	val sentAt = long("sent_at")
}