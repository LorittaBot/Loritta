package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object LorittaAddedGuildsStats : LongIdTable() {
    val guildId = long("guild_id")
    val addedAt = timestampWithTimeZone("added_at").index()
    val addedBy = long("added_by")

    val sourceValue = text("source").nullable().index()
    val medium = text("medium").nullable().index()
    val campaign = text("campaign").nullable().index()
    val content = text("content").nullable().index()

    val httpReferrer = text("http_referrer").nullable().index()
    val discordLocale = text("discord_locale").index()
}