package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone

object UserPocketLorittaSettings : SnowflakeTable() {
    val lorittaCount = integer("loritta_count")
    val pantufaCount = integer("pantufa_count")
    val gabrielaCount = integer("gabriela_count")
    val updatedAt = timestampWithTimeZone("updated_at")
}