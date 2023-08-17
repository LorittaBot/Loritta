package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.BackgroundVariation
import net.perfectdreams.loritta.serializable.DefaultBackgroundVariation
import net.perfectdreams.loritta.serializable.ProfileDesignGroupBackgroundVariation

class PuddingBackground(
    private val pudding: Pudding,
    val data: Background
) {
    companion object;

    val id by data::id

    suspend fun getVariations() = pudding.backgrounds.getBackgroundVariations(data.id)
    suspend fun getVariationForProfileDesign(internalName: String): BackgroundVariation {
        val profileDesignGroupIds = pudding.profileDesigns.getProfileDesignGroupIdsOfDesign(internalName)
        val variations = getVariations()
        // If there isn't a profile design group ID matching the internalName, return the default variation
        val profileDesignGroupId = profileDesignGroupIds.firstOrNull() ?: return variations.firstOrNull { it is DefaultBackgroundVariation } ?: error("Background \"${data.id}\" does not have a default profile design variation!")
        return getVariationForProfileDesignGroup(profileDesignGroupId.toString(), getVariations())
    }

    fun getVariationForProfileDesignGroup(profileDesignGroupId: String, variations: List<BackgroundVariation>): BackgroundVariation {
        if (variations.isEmpty()) // Empty list, this means that the background is invalid and should fail
            error("Background \"${data.id}\" does not have a profile design group variation!")

        // Try getting the variation that matches the current profile design group ID
        for (variation in variations) {
            if (variation is ProfileDesignGroupBackgroundVariation && variation.profileDesignGroupId == profileDesignGroupId)
                return variation
        }

        // If everything fails, try getting the default variation (or null)
        return variations.firstOrNull { it is DefaultBackgroundVariation } ?: error("Background \"${data.id}\" does not have a default profile design variation!")
    }
}