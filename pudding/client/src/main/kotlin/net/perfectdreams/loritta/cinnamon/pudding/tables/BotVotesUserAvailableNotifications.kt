package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.common.utils.WebsiteVoteSource
import org.jetbrains.exposed.dao.id.LongIdTable

object BotVotesUserAvailableNotifications : LongIdTable() {
    val userId = long("user")
    val botVote = reference("bot_vote", BotVotes)
    val notifyAt = timestampWithTimeZone("notify_at").index()
    val notified = bool("notified").index()
}