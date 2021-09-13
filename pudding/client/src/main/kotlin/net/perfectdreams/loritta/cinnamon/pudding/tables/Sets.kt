package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object Sets : IdTable<String>() {
    val internalName = text("internal_name")
    override val id: Column<EntityID<String>> = internalName.entityId()

    override val primaryKey = PrimaryKey(id)
}