package net.perfectdreams.loritta.morenitta.websitedashboard.routes.dailyshop

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.serializable.DailyShopBackgroundEntry
import net.perfectdreams.loritta.serializable.ProfileDesign
import net.perfectdreams.loritta.serializable.ProfileSectionsResponse

class DailyShopResult(
    val profile: Profile?,
    val activeProfileDesignId: String,
    val activeBackgroundId: String,
    val backgrounds: List<DailyShopBackgroundEntry>,
    val profileDesigns: List<ProfileDesign>,
    val backgroundsWrapper: ProfileSectionsResponse.BackgroundsWrapper,
    val boughtProfileDesigns: List<ProfileDesign>,
    val generatedAt: Long
)