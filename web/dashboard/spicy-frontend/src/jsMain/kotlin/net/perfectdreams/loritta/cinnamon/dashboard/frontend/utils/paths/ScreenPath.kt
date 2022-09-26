package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths

import net.perfectdreams.loritta.cinnamon.dashboard.common.RoutePaths
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.Screen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ShipEffectsScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.SonhosShopScreen

sealed class ScreenPath {
    object ShipEffectsScreenPath : ScreenPath() {
        override fun matches(path: String) = path == build()

        override fun createScreen(m: LorittaDashboardFrontend, path: String) = ShipEffectsScreen(m)

        override fun build() = RoutePaths.SHIP_EFFECTS
    }

    object SonhosShopScreenPath : ScreenPath() {
        override fun matches(path: String) = path == build()

        override fun createScreen(m: LorittaDashboardFrontend, path: String) = SonhosShopScreen(m)

        override fun build() = RoutePaths.SONHOS_SHOP
    }

    abstract fun matches(path: String): Boolean
    abstract fun createScreen(m: LorittaDashboardFrontend, path: String): Screen
    abstract fun build(): String

    companion object {
        val all = listOf(
            ShipEffectsScreenPath,
            SonhosShopScreenPath
        )
    }
}