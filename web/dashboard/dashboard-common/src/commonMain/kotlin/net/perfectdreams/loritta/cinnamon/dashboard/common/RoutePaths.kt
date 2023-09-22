package net.perfectdreams.loritta.cinnamon.dashboard.common

object RoutePaths {
    val GUILDS = listOf(
        ScreenPathElement.StringPathElement("users"),
        ScreenPathElement.StringPathElement("@me"),
        ScreenPathElement.StringPathElement("guilds")
    )

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

    val GUILD_WELCOMER_CONFIG = listOf(
        ScreenPathElement.StringPathElement("guilds"),
        ScreenPathElement.OptionPathElement("guildId"),
        ScreenPathElement.StringPathElement("configure"),
        ScreenPathElement.StringPathElement("welcomer")
    )

    val GUILD_STARBOARD_CONFIG = listOf(
        ScreenPathElement.StringPathElement("guilds"),
        ScreenPathElement.OptionPathElement("guildId"),
        ScreenPathElement.StringPathElement("configure"),
        ScreenPathElement.StringPathElement("starboard")
    )

    val GUILD_CUSTOM_COMMANDS_CONFIG = listOf(
        ScreenPathElement.StringPathElement("guilds"),
        ScreenPathElement.OptionPathElement("guildId"),
        ScreenPathElement.StringPathElement("configure"),
        ScreenPathElement.StringPathElement("custom-commands")
    )

    val ADD_NEW_GUILD_CUSTOM_COMMAND_CONFIG = listOf(
        ScreenPathElement.StringPathElement("guilds"),
        ScreenPathElement.OptionPathElement("guildId"),
        ScreenPathElement.StringPathElement("configure"),
        ScreenPathElement.StringPathElement("custom-commands"),
        ScreenPathElement.StringPathElement("add")
    )

    val EDIT_GUILD_CUSTOM_COMMAND_CONFIG = listOf(
        ScreenPathElement.StringPathElement("guilds"),
        ScreenPathElement.OptionPathElement("guildId"),
        ScreenPathElement.StringPathElement("configure"),
        ScreenPathElement.StringPathElement("custom-commands"),
        ScreenPathElement.OptionPathElement("commandId"),
        ScreenPathElement.StringPathElement("edit"),
    )
}