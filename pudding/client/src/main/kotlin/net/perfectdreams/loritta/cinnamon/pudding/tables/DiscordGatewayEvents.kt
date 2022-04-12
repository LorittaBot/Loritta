package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object DiscordGatewayEvents : LongIdTable() {
    val type = text("type").index()
    val payload = jsonb("payload")
}