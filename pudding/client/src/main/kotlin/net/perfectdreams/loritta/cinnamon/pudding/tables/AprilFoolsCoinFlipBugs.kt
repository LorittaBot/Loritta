package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object AprilFoolsCoinFlipBugs : LongIdTable()  {
    val userId = long("user").index()
    val bug = text("bug")
    val beggedAt = timestampWithTimeZone("begged_at").index()
    val year = integer("year").index()
}