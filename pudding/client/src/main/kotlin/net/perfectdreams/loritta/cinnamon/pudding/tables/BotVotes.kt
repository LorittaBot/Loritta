package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.common.utils.LegacyWebsiteVoteSource
import org.jetbrains.exposed.dao.id.LongIdTable

object BotVotes : LongIdTable() {
    val userId = long("user").index()
    val websiteSource = enumeration("website_source", LegacyWebsiteVoteSource::class)
    val votedAt = long("created_at")
}