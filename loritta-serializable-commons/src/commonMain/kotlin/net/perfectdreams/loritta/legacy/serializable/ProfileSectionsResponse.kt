package net.perfectdreams.loritta.legacy.serializable

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.pudding.data.BackgroundWithVariations

@Serializable
data class ProfileSectionsResponse(
    val profile: ProfileDataWrapper?,
    val donations: DonationsWrapper?,
    val settings: SettingsWrapper?,
    val backgrounds: BackgroundsWrapper?,
    val profileDesigns: List<ProfileDesign>?
) {
    @Serializable
    data class ProfileDataWrapper(
        val xp: Long,
        val money: Long
    )

    @Serializable
    data class DonationsWrapper(
        val value: Double
    )

    @Serializable
    data class SettingsWrapper(
        val activeBackground: String?,
        val activeProfileDesign: String?
    )

    @Serializable
    data class BackgroundsWrapper(
        val dreamStorageServiceUrl: String,
        val dreamStorageServiceNamespace: String,
        val backgrounds: List<BackgroundWithVariations>
    )

    @Serializable
    data class ProfileDesignsWrapper(
        val dreamStorageServiceUrl: String,
        val dreamStorageServiceNamespace: String,
        val backgrounds: List<BackgroundWithVariations>
    )
}