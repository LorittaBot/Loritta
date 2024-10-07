package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberCharacterInventoryItems : LongIdTable() {
    val owner = reference("owner", LoriTuberCharacters)
    val item = text("item")
    val addedAt = timestampWithTimeZone("added_at")
}