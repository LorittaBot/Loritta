package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.Column

open class SnowflakeTable(name: String = "", columnName: String = "id") : IdTable<Long>(name) {
	override val id: Column<EntityID<Long>> = long(columnName).primaryKey().entityId()
}

abstract class SnowflakeEntity(id: EntityID<Long>) : Entity<Long>(id)

abstract class SlowflakeEntityClass<out E: LongEntity>(table: IdTable<Long>, entityType: Class<E>? = null) : EntityClass<Long, E>(table, entityType)
