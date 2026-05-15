package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object CollectionItems : LongIdTable() {
    val collection = reference("collection", Collections).index()
    val background = optReference("background", Backgrounds)
    val profileDesign = optReference("profile_design", ProfileDesigns)
}
