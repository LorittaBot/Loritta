package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object CollectionRewardProfileDesigns : LongIdTable() {
    val collection = reference("collection", Collections).index()
    val profileDesign = reference("profile_design", ProfileDesigns)
}
