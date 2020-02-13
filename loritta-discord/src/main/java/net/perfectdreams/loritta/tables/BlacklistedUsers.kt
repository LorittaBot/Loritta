package net.perfectdreams.loritta.tables

import com.mrpowergamerbr.loritta.utils.networkbans.NetworkBanType
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

object BlacklistedUsers : IdTable<Long>() {
	override val id: Column<EntityID<Long>> = long("user").primaryKey().entityId()

	val bannedAt = long("banned_at")
	val guildId = long("guild").nullable()
	val type = enumeration("type", NetworkBanType::class)
	val reason = text("reason")
	val globally = bool("globally").index().default(false)
}