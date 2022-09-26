package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.Rarity

@Serializable
data class Background(
    val id: String,
    val enabled: Boolean,
    val rarity: Rarity,
    val createdBy: List<String>,
    val set: String?
) {
    companion object {
        const val DEFAULT_BACKGROUND_ID = "defaultBlue"
        const val RANDOM_BACKGROUND_ID = "random"
        const val CUSTOM_BACKGROUND_ID = "custom"
    }
}