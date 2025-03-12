package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.Sets
import net.perfectdreams.loritta.cinnamon.pudding.tables.Sets.entityId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

object LoriTuberServerInfos : IdTable<String>() {
    val type = text("type")
    override val id: Column<EntityID<String>> = type.entityId()

    val data = jsonb("data")

    override val primaryKey = PrimaryKey(id)
}