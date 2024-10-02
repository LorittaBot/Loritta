package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberVideoTickEvents : LongIdTable() {
    val video = reference("video", LoriTuberVideos).index()
    val processed = bool("processed").index()
    val runAtTick = long("run_at_tick").index()
    val event = jsonb("event")
}