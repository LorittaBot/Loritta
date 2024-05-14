package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.serializable.ColorTheme

object UserWebsiteSettings : SnowflakeTable() {
    val dashboardColorThemePreference = postgresEnumeration<ColorTheme>("dashboard_color_theme_preference").nullable()
    val dashboardColorThemePreferenceUpdatedAt = timestampWithTimeZone("dashboard_color_theme_preference_updated_at").nullable()
}