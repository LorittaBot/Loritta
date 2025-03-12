package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object TwitchEventSubEvents : LongIdTable() {
    val event = jsonb("event")
}