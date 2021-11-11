package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object ProfileDesignGroupEntries : LongIdTable() {
    val profileDesign = reference("profile_design", ProfileDesigns)
    val profileDesignGroup = reference("profile_design_group", ProfileDesignGroups)

    init {
        uniqueIndex(profileDesign, profileDesignGroup)
    }
}