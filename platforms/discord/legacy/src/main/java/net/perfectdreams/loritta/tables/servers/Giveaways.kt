package net.perfectdreams.loritta.tables.servers

import com.mrpowergamerbr.loritta.utils.exposed.array
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.TextColumnType

object Giveaways : LongIdTable() {
	val guildId = long("guild").index()
	val textChannelId = long("channel")
	val messageId = long("message")

	val reason = text("reason")
	val description = text("description")
	val numberOfWinners = integer("number_of_winners")
	val reaction = text("reaction")
	val finishAt = long("finish_at")
	val customMessage = text("custom_message").nullable()
	val locale = text("locale")
	val roleIds = array<String>("roles", TextColumnType()).nullable()
	val finished = bool("finished").default(false).index()
}