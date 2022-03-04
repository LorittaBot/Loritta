package net.perfectdreams.dokyo

import kotlinx.serialization.Serializable

@Serializable
enum class WebsiteTheme(val bodyClass: String, val icon: String) {
    DEFAULT("light", "fas fa-moon"),
    DARK_THEME("dark", "fas fa-sun")
}