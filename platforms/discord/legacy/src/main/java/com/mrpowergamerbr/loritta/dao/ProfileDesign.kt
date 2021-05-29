package com.mrpowergamerbr.loritta.dao

import net.perfectdreams.loritta.tables.ProfileDesigns
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ProfileDesign(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, ProfileDesign>(ProfileDesigns) {
        const val DEFAULT_PROFILE_DESIGN_ID = "defaultDark"
        const val RANDOM_PROFILE_DESIGN_ID = "random"
    }

    var enabled by ProfileDesigns.enabled
    var rarity by ProfileDesigns.rarity
    var createdBy by ProfileDesigns.createdBy
    var availableToBuyViaDreams by ProfileDesigns.availableToBuyViaDreams
    var availableToBuyViaMoney by ProfileDesigns.availableToBuyViaMoney
    var set by ProfileDesigns.set

    fun toSerializable() = net.perfectdreams.loritta.serializable.ProfileDesign(
            id.value,
            enabled,
            rarity,
            createdBy.toList(),
            set?.value
    )
}