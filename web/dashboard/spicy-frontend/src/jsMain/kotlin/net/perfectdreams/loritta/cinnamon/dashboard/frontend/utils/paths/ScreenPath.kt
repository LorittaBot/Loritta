package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths

import net.perfectdreams.loritta.cinnamon.dashboard.common.RoutePaths
import net.perfectdreams.loritta.cinnamon.dashboard.common.ScreenPathElement
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.*

sealed class ScreenPath(val elements: List<ScreenPathElement>) {
    object ChooseAServerScreenPath : ScreenPath(RoutePaths.GUILDS) {
        override fun createScreen(
            m: LorittaDashboardFrontend,
            currentScreen: Screen?,
            path: String,
            parsedArguments: Map<String, String>
        ): Screen = ChooseAServerScreen(m)
    }

    object ShipEffectsScreenPath : ScreenPath(RoutePaths.SHIP_EFFECTS) {
        override fun createScreen(
            m: LorittaDashboardFrontend,
            currentScreen: Screen?,
            path: String,
            parsedArguments: Map<String, String>
        ): Screen = ShipEffectsScreen(m)
    }

    object SonhosShopScreenPath : ScreenPath(RoutePaths.SONHOS_SHOP) {
        override fun createScreen(
            m: LorittaDashboardFrontend,
            currentScreen: Screen?,
            path: String,
            parsedArguments: Map<String, String>
        ): Screen = SonhosShopScreen(m)
    }

    object ConfigureGuildGamerSaferVerifyPath : ScreenPath(RoutePaths.GUILD_GAMERSAFER_CONFIG) {
        override fun createScreen(
            m: LorittaDashboardFrontend,
            currentScreen: Screen?,
            path: String,
            parsedArguments: Map<String, String>
        ) = ConfigureGuildGamerSaferVerifyScreen(m, parsedArguments["guildId"]!!.toLong())
    }

    fun matches(path: String): ScreenPathMatchResult {
        val split = path.split("/").drop(1)
        val parsedArguments = mutableMapOf<String, String>()
        for ((index, e) in split.withIndex()) {
            val el = elements.getOrNull(index) ?: return ScreenPathMatchResult.Failure

            when (el) {
                is ScreenPathElement.OptionPathElement -> {
                    parsedArguments[el.parameterId] = e
                }
                is ScreenPathElement.StringPathElement -> {
                    if (e != el.text)
                        return ScreenPathMatchResult.Failure
                }
            }
        }
        return ScreenPathMatchResult.Success(parsedArguments)
    }

    abstract fun createScreen(
        m: LorittaDashboardFrontend,
        currentScreen: Screen?,
        path: String,
        parsedArguments: Map<String, String>
    ): Screen

    sealed class ScreenPathMatchResult {
        object Failure : ScreenPathMatchResult()
        class Success(val parsedArguments: Map<String, String>) : ScreenPathMatchResult()
    }

    companion object {
        val all = listOf(
            ChooseAServerScreenPath,
            ShipEffectsScreenPath,
            SonhosShopScreenPath,
            ConfigureGuildGamerSaferVerifyPath
        )
    }
}