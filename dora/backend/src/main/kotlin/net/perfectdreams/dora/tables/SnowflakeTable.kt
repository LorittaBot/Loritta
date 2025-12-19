package net.perfectdreams.dora.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

open class SnowflakeTable(name: String = "", columnName: String = "id") : IdTable<Long>(name) {
    override val id: Column<EntityID<Long>> = long(columnName).entityId()
    override val primaryKey = PrimaryKey(id)
}

open class UniqueSnowflakeTable(name: String = "", columnName: String = "id") : IdTable<Long>(name) {
    override val id: Column<EntityID<Long>> = long(columnName).entityId().uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}