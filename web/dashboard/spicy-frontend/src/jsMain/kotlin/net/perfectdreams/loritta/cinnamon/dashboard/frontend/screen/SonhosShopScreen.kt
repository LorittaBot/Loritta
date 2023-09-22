package net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen

import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.i18n.I18nKeysData

class SonhosShopScreen(m: LorittaDashboardFrontend) : UserScreen(m) {
    override fun createPathWithArguments() = ScreenPathWithArguments(
        ScreenPath.SonhosShopScreenPath,
        mapOf(),
        mapOf()
    )
    override fun createTitle() = I18nKeysData.Website.Dashboard.SonhosShop.Title
}