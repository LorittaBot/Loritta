package net.perfectdreams.loritta.cinnamon.pudding.tables.bomdiaecia

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import org.jetbrains.exposed.dao.id.LongIdTable

object BomDiaECiaMatchWinners : LongIdTable() {
    val match = reference("match", BomDiaECiaMatches)
    val user = reference("user", Profiles)
    val wonAt = timestampWithTimeZone("won_at")
    val prize = long("prize")
}