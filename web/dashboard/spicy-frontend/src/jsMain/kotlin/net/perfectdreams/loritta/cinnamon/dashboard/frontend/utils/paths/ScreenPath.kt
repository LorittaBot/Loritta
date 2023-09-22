package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths

import io.ktor.http.*
import net.perfectdreams.loritta.cinnamon.dashboard.common.RoutePaths
import net.perfectdreams.loritta.cinnamon.dashboard.common.ScreenPathElement
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.*
import org.w3c.dom.url.URLSearchParams

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

    object ConfigureGuildWelcomerPath : ScreenPath(RoutePaths.GUILD_WELCOMER_CONFIG) {
        override fun createScreen(
            m: LorittaDashboardFrontend,
            currentScreen: Screen?,
            path: String,
            parsedArguments: Map<String, String>
        ) = ConfigureGuildWelcomerScreen(m, parsedArguments["guildId"]!!.toLong())
    }

    object ConfigureGuildStarboardPath : ScreenPath(RoutePaths.GUILD_STARBOARD_CONFIG) {
        override fun createScreen(
            m: LorittaDashboardFrontend,
            currentScreen: Screen?,
            path: String,
            parsedArguments: Map<String, String>
        ) = ConfigureGuildStarboardScreen(m, parsedArguments["guildId"]!!.toLong())
    }

    object ConfigureGuildCustomCommandsPath : ScreenPath(RoutePaths.GUILD_CUSTOM_COMMANDS_CONFIG) {
        override fun createScreen(
            m: LorittaDashboardFrontend,
            currentScreen: Screen?,
            path: String,
            parsedArguments: Map<String, String>
        ) = ConfigureGuildCustomCommandsScreen(m, parsedArguments["guildId"]!!.toLong())
    }

    object AddNewGuildCustomCommandPath : ScreenPath(RoutePaths.ADD_NEW_GUILD_CUSTOM_COMMAND_CONFIG) {
        override fun createScreen(
            m: LorittaDashboardFrontend,
            currentScreen: Screen?,
            path: String,
            parsedArguments: Map<String, String>
        ) = AddNewGuildCustomCommandScreen(m, parsedArguments["guildId"]!!.toLong(), URLSearchParams(Url(path).encodedQuery).get("type")!!)
    }

    object EditGuildCustomCommandPath : ScreenPath(RoutePaths.EDIT_GUILD_CUSTOM_COMMAND_CONFIG) {
        override fun createScreen(
            m: LorittaDashboardFrontend,
            currentScreen: Screen?,
            path: String,
            parsedArguments: Map<String, String>
        ) = EditGuildCustomCommandScreen(m, parsedArguments["guildId"]!!.toLong(), parsedArguments["commandId"]!!.toLong())
    }

    fun matches(path: String): ScreenPathMatchResult {
        val split = path.split("/").drop(1)
        if (split.size != elements.size)
            return ScreenPathMatchResult.Failure // Doesn't match, missing elements!

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
            ConfigureGuildGamerSaferVerifyPath,
            ConfigureGuildWelcomerPath,
            ConfigureGuildStarboardPath,
            ConfigureGuildCustomCommandsPath,
            AddNewGuildCustomCommandPath,
            EditGuildCustomCommandPath
        )
    }
}