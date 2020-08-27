package net.perfectdreams.loritta.tables

import com.google.gson.Gson
import com.mrpowergamerbr.loritta.utils.exposed.array
import com.mrpowergamerbr.loritta.utils.exposed.rawJsonb
import net.perfectdreams.loritta.api.utils.Rarity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.TextColumnType

object ProfileDesigns : IdTable<String>() {
    val internalName = text("internal_name").primaryKey()
    override val id: Column<EntityID<String>> = internalName.entityId()

    val enabled = bool("enabled").index()
    val rarity = enumeration("rarity", Rarity::class).index()
    val createdBy = array<String>("created_by", TextColumnType())
    val availableToBuyViaDreams = bool("available_to_buy_via_dreams").index()
    val availableToBuyViaMoney = bool("available_to_buy_via_money").index()
    val set = optReference("set", Sets)
    val addedAt = long("added_at").default(-1L)
}