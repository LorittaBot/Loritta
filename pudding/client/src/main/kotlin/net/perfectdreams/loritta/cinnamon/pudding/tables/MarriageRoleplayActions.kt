package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.common.utils.RoleplayAction
import org.jetbrains.exposed.dao.id.LongIdTable

object MarriageRoleplayActions : LongIdTable() {
    val marriage = reference("marriage", UserMarriages).index()
    val action = enumerationByName<RoleplayAction>("action", 64).index()
    val sentAt = timestampWithTimeZone("sent_at").index()
    val sentBy = long("sent_by_id").index()
    val affinityReward = bool("affinity_reward")
}