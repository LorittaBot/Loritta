package net.perfectdreams.loritta.plugin.malcommands.util

import java.awt.Color

object MalConstants {
    val MAL_COLOR = Color(46, 81, 162)
    const val MAL_URL = "https://myanimelist.net/"
    val MAL_ANIMEURL_REGEX = Regex("^(http|https)://myanimelist\\.net/anime/\\d*/\\w*/?$")
}