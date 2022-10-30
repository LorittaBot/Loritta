package net.perfectdreams.loritta.morenitta.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object BlacklistedGuilds : IdTable<Long>() {
    override val id: Column<EntityID<Long>> = long("guild").entityId()

    val bannedAt = long("banned_at")
    val reason = text("reason")

    override val primaryKey = PrimaryKey(id)
}