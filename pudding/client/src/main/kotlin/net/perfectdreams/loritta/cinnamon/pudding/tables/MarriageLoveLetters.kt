package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object MarriageLoveLetters : LongIdTable() {
    val marriage = reference("marriage", UserMarriages).index()
    val content = text("content")
    val sentAt = timestampWithTimeZone("sent_at").index()
    val sentBy = long("sent_by_id")
    val affinityReward = bool("affinity_reward")
}