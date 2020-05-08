package net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.tables

import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.utils.PunishmentCategory
import org.jetbrains.exposed.dao.id.LongIdTable

object UserReports : LongIdTable() {
	val userId = long("user").index()
	val reportedBy = long("reported_by").index()
	val guildId = long("guild").index()
	val reportedAt = long("reported_at")
	val revoked = bool("revoked")
	val approved = bool("approved")
	val category = enumeration("category", PunishmentCategory::class)
	val reason = text("reason")
}