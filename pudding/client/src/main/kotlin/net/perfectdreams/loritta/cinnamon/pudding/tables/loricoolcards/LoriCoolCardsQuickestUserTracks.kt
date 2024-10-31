package net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards

import org.jetbrains.exposed.dao.id.LongIdTable

object LoriCoolCardsQuickestUserTracks : LongIdTable() {
    val userId = long("user").index()
    val finished = reference("finished", LoriCoolCardsFinishedAlbumUsers)
    val type = integer("type")
}