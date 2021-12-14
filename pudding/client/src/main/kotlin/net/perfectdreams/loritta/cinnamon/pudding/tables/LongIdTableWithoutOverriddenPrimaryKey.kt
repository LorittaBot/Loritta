package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

/**
 * A [LongIdTable] without overridden primary key
 */
open class LongIdTableWithoutOverriddenPrimaryKey(name: String = "", columnName: String = "id") : IdTable<Long>(name) {
    final override val id: Column<EntityID<Long>> = long(columnName).autoIncrement().entityId()
}