package net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object CollectedChristmas2022Points : LongIdTable() {
    val user = reference("user", Christmas2022Players).index()
    val message = reference("message", Christmas2022Drops).index()
    val points = integer("points")
    val collectedAt = timestampWithTimeZone("collected_at")
}