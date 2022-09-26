package net.perfectdreams.loritta.legacy.tables

import net.perfectdreams.loritta.legacy.utils.WebsiteVoteSource
import org.jetbrains.exposed.dao.id.LongIdTable

object BotVotes : LongIdTable() {
    val userId = long("user").index()
    val websiteSource = enumeration("website_source", WebsiteVoteSource::class)
    val votedAt = long("created_at")
}