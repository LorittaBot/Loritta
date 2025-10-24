package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.shimeji.ActivityLevel

object UserPocketLorittaSettings : SnowflakeTable() {
    val lorittaCount = integer("loritta_count")
    val pantufaCount = integer("pantufa_count")
    val gabrielaCount = integer("gabriela_count")
    val activityLevel = enumerationByName<ActivityLevel>("activity_level", 64)
    val updatedAt = timestampWithTimeZone("updated_at")
}