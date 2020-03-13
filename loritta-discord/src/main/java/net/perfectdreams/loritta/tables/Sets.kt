package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

object Sets : IdTable<String>() {
    val internalName = text("internal_name").primaryKey()
    override val id: Column<EntityID<String>> = internalName.entityId()
}