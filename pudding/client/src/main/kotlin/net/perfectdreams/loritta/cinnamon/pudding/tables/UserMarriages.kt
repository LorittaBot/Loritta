package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object UserMarriages : LongIdTable() {
    val createdAt = timestampWithTimeZone("created_at").index()
    val active = bool("active").index()
    val expiredAt = timestampWithTimeZone("expired_at").nullable().index()
    // val affinity = integer("affinity")
    val hugCount = integer("hug_count")
    val headPatCount = integer("head_pat_count")
    val highFiveCount = integer("high_five_count")
    val slapCount = integer("slap_count")
    val attackCount = integer("attack_count")
    val danceCount = integer("dance_count")
    val kissCount = integer("kiss_count")
    val coupleName = text("couple_name").nullable()
    val coupleBadge = uuid("couple_badge").nullable()
}