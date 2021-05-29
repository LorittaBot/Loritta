package net.perfectdreams.loritta.tables

import net.perfectdreams.loritta.utils.WebsiteVoteSource
import org.jetbrains.exposed.dao.id.LongIdTable

object BotVotes : LongIdTable() {
    val userId = long("user").index()
    val websiteSource = enumeration("website_source", WebsiteVoteSource::class)
    val votedAt = long("created_at")
}