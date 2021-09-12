package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.common.utils.Rarity
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.array
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.TextColumnType

object Backgrounds : IdTable<String>() {
    val internalName = text("internal_name")
    override val id: Column<EntityID<String>> = internalName.entityId()

    val imageFile = text("image_file")
    val enabled = bool("enabled").index()
    val rarity = enumeration("rarity", Rarity::class).index()
    val createdBy = array<String>("created_by", TextColumnType())
    val crop = jsonb("crop").nullable()
    val availableToBuyViaDreams = bool("available_to_buy_via_dreams").index()
    val availableToBuyViaMoney = bool("available_to_buy_via_money").index()
    val set = optReference("set", Sets)
    val addedAt = long("added_at").default(-1L)

    override val primaryKey = PrimaryKey(id)
}