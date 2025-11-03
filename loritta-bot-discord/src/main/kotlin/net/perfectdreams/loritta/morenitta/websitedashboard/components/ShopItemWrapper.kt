package net.perfectdreams.loritta.morenitta.websitedashboard.components

import net.perfectdreams.loritta.common.utils.Rarity
import net.perfectdreams.loritta.serializable.DailyShopBackgroundEntry
import net.perfectdreams.loritta.serializable.ProfileDesign
import net.perfectdreams.loritta.serializable.ProfileSectionsResponse


sealed class ShopItemWrapper {
    abstract val internalName: String
    abstract val rarity: Rarity
    abstract val tag: String?
    abstract val localePrefix: String?
    abstract val price: Int?
    abstract val set: String?
    abstract val createdBy: List<String>?
}

class BackgroundItemWrapper(backgroundEntry: DailyShopBackgroundEntry) : ShopItemWrapper() {
    val background = backgroundEntry.backgroundWithVariations.background
    val variations = backgroundEntry.backgroundWithVariations.variations
    override val internalName = background.id
    override val rarity = background.rarity
    override val tag = backgroundEntry.tag
    override val localePrefix = "backgrounds"
    override val price = rarity.getBackgroundPrice()
    override val set = backgroundEntry.backgroundWithVariations.background.set
    override val createdBy = backgroundEntry.backgroundWithVariations.background.createdBy

    /**
     * Checks if the user has already bought the item or not
     *
     * @return if the user already has the item
     */
    fun hasBought(backgroundsWrapper: ProfileSectionsResponse.BackgroundsWrapper) = backgroundsWrapper.backgrounds.any { it.background.id == internalName }
}

class ProfileDesignItemWrapper(val profileDesign: ProfileDesign) : ShopItemWrapper() {
    override val internalName = profileDesign.internalName
    override val rarity = profileDesign.rarity
    override val tag = profileDesign.tag
    override val localePrefix = "profileDesigns"
    override val price = rarity.getProfilePrice()
    override val set = profileDesign.set
    override val createdBy = profileDesign.createdBy

    /**
     * Checks if the user has already bought the item or not
     *
     * @return if the user already has the item
     */
    fun hasBought(profileDesigns: List<ProfileDesign>) = profileDesigns.any { it.internalName == internalName }
}