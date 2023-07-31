package net.perfectdreams.loritta.cinnamon.dashboard.common

object RoutePaths {
    val SHIP_EFFECTS = listOf(
        ScreenPathElement.StringPathElement("users"),
        ScreenPathElement.StringPathElement("@me"),
        ScreenPathElement.StringPathElement("ship-effects")
    )

    val SONHOS_SHOP = listOf(
        ScreenPathElement.StringPathElement("users"),
        ScreenPathElement.StringPathElement("@me"),
        ScreenPathElement.StringPathElement("sonhos-shop")
    )

    val GUILD_GAMERSAFER_CONFIG = listOf(
        ScreenPathElement.StringPathElement("guilds"),
        ScreenPathElement.OptionPathElement("guildId"),
        ScreenPathElement.StringPathElement("configure"),
        ScreenPathElement.StringPathElement("gamersafer-verify")
    )
}