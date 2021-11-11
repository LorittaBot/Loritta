package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignGroupEntries
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignGroups
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesigns
import org.jetbrains.exposed.sql.select

class ProfileDesignsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getProfileDesignGroupIdsOfDesign(internalName: String) = pudding.transaction {
        ProfileDesignGroups.innerJoin(ProfileDesignGroupEntries)
            .innerJoin(ProfileDesigns)
            .select {
                ProfileDesigns.internalName eq internalName
            }.map { it[ProfileDesignGroups.id].value }
    }
}