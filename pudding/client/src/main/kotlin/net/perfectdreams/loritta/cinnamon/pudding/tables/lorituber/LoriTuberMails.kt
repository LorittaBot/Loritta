package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberMails : LongIdTable() {
    val character = reference("character", LoriTuberCharacters).index()
    val date = timestampWithTimeZone("date").index()
    val type = jsonb("type")
    val acknowledged = bool("acknowledged").index()
}