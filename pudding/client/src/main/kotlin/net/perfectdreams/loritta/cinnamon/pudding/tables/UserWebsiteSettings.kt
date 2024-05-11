package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.jsonb

object UserWebsiteSettings : SnowflakeTable() {
    val favoritedGuilds = jsonb("favorited_guilds").nullable()
}