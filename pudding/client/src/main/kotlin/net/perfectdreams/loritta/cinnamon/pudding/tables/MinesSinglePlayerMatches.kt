package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import net.perfectdreams.loritta.common.utils.MinesResult
import org.jetbrains.exposed.dao.id.LongIdTable

object MinesSinglePlayerMatches : LongIdTable() {
    val lorittaClusterId = integer("cluster_id").index()
    val minesManagerUniqueId = uuid("mines_manager_unique_id").index()
    val user = long("user").index()
    val guild = long("guild").nullable().index()
    val channel = long("channel").index()
    val totalMines = integer("total_mines")
    val pickedTiles = integer("picked_tiles").nullable()
    val bet = long("bet").nullable()
    val payout = long("payout").nullable()
    val refunded = bool("refunded")
    val autoStand = bool("auto_stand")
    val startedAt = timestampWithTimeZone("started_at").index()
    val finishedAt = timestampWithTimeZone("finished_at").nullable().index()
    val result = enumerationByName<MinesResult>("result", 64).nullable()
    val playfield = jsonb("playfield").nullable()
    val pickedPlayfield = jsonb("picked_playfield").nullable()
    val lastBombX = integer("last_tile_x").nullable()
    val lastBombY = integer("last_tile_y").nullable()
}