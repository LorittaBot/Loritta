package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object DropChats : LongIdTable() {
    val guildId = long("guild_id").index()
    val channelId = long("channel_id").index()
    val messageId = long("message_id").index()
    val startedById = long("started_by_id").index()
    val moneySourceId = long("money_source_id").nullable().index()
    val startedAt = timestampWithTimeZone("started_at").index()
    val endedAt = timestampWithTimeZone("ended_at").index()
    val participantPayout = long("participant_payout")
    val maxParticipants = integer("max_participants")
    val maxWinners = integer("max_winners")
    val participants = integer("participants")
    val winners = integer("winners")
}