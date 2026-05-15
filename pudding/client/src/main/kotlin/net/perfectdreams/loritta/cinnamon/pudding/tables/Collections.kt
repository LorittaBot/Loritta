package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object Collections : IdTable<String>() {
    val internalName = text("internal_name")
    override val id: Column<EntityID<String>> = internalName.entityId()

    val enabled = bool("enabled").index()
    val rewardSonhos = long("reward_sonhos")
    val addedAt = timestampWithTimeZone("added_at")

    override val primaryKey = PrimaryKey(id)
}
