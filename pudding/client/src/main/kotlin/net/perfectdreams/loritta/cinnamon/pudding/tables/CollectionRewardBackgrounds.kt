package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object CollectionRewardBackgrounds : LongIdTable() {
    val collection = reference("collection", Collections).index()
    val background = reference("background", Backgrounds)
}
