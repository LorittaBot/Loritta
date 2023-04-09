package net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.common.utils.easter2023.EasterEggColor
import org.jetbrains.exposed.dao.id.LongIdTable

object CollectedEaster2023Eggs : LongIdTable() {
    val user = reference("user", Easter2023Players).index()
    val message = reference("message", Easter2023Drops).index()
    val points = integer("points")
    val collectedAt = timestampWithTimeZone("collected_at")
    val valid = bool("valid").default(true)
    val associatedWithBasket = optReference("associated_with_basket", CreatedEaster2023Baskets)
}